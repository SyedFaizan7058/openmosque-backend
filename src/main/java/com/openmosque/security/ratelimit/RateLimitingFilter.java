package com.openmosque.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openmosque.common.model.ApiResponse;
import com.openmosque.security.model.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that enforces Token Bucket rate limiting across public, authenticated, and sensitive endpoints.
 * Resolves client identity (IP or Authenticated User ID) and appends RFC rate limiting headers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip health checks, swagger docs, and static assets
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = resolveClientIp(request);
        UUID userId = resolveAuthenticatedUserId();

        RateLimitResult result;

        // 1. Check for Sensitive Write Endpoints (15 req/min)
        if (isSensitiveWriteEndpoint(path, method)) {
            String key = userId != null
                    ? "sensitive:user:" + userId
                    : "sensitive:ip:" + clientIp;
            result = rateLimiterService.tryConsume(key, properties.getSensitiveWriteLimit());
        }
        // 2. Check for Authenticated Endpoints (120 req/min)
        else if (userId != null) {
            String key = "auth-user:" + userId;
            result = rateLimiterService.tryConsume(key, properties.getAuthenticatedLimit());
        }
        // 3. Public Read / Default Endpoints (100 req/min per IP)
        else {
            String key = "public-ip:" + clientIp;
            result = rateLimiterService.tryConsume(key, properties.getPublicReadLimit());
        }

        // Always append rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, result.getRemaining())));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetSeconds()));

        if (!result.isAllowed()) {
            response.setHeader("Retry-After", String.valueOf(Math.max(1, result.getRetryAfterSeconds())));
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiResponse<Void> errorResponse = ApiResponse.error(
                    "RATE_LIMIT_EXCEEDED",
                    "Too many requests. Please try again later in " + result.getRetryAfterSeconds() + " seconds."
            );
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSensitiveWriteEndpoint(String path, String method) {
        if (!HttpMethod.POST.matches(method) && !HttpMethod.PATCH.matches(method)) {
            return false;
        }

        return pathMatcher.match("/api/v1/auth/2fa/**", path)
                || pathMatcher.match("/api/v1/auth/session", path)
                || pathMatcher.match("/api/v1/users/sync", path)
                || pathMatcher.match("/api/v1/media/**", path)
                || pathMatcher.match("/api/v1/mosques/submissions", path)
                || pathMatcher.match("/api/v1/mosques/*/suggest-edit", path)
                || pathMatcher.match("/api/v1/mosques/*/reviews", path)
                || pathMatcher.match("/api/v1/mosques/*/claim", path)
                || pathMatcher.match("/api/v1/community/flag", path)
                || pathMatcher.match("/api/v1/mosques/*/questions", path)
                || pathMatcher.match("/api/v1/community/questions/*/answers", path)
                || pathMatcher.match("/api/v1/users/me/notifications/devices", path);
    }

    private UUID resolveAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    /**
     * Resolves client IP address securely.
     * Only trusts X-Forwarded-For if request originated from an explicitly configured trusted reverse proxy.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        if (properties.getTrustedProxies() != null && properties.getTrustedProxies().contains(remoteAddr)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                // Return first hop IP
                return forwarded.split(",")[0].trim();
            }
        }

        return (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr : "127.0.0.1";
    }
}
