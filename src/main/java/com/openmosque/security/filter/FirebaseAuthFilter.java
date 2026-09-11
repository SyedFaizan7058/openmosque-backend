package com.openmosque.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.repository.UserRepository;
import com.openmosque.security.model.CustomUserDetails;
import com.openmosque.security.service.FirebaseTokenVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security Filter for Firebase JWT Bearer Token Verification.
 * 
 * SECURITY HARDENING:
 * 1. Strictly rejects mock tokens in production environment.
 * 2. Blocks auto-role escalation: All new real users are strictly assigned UserRole.USER.
 * 3. Immediately terminates requests from suspended or soft-deleted accounts.
 * 4. Extracts Bearer token from header or HttpOnly secure cookies.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final FirebaseTokenVerifier tokenVerifier;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.security.firebase.dev-mock-auth:false}")
    private boolean devMockAuthEnabled;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract Bearer token from header or HttpOnly cookie
        String token = extractBearerToken(request);

        // 2. If token exists and context is not already authenticated, verify it
        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            FirebaseTokenVerifier.VerifiedTokenInfo tokenInfo = tokenVerifier.verifyToken(token);

            if (tokenInfo != null) {
                boolean isProd = activeProfile != null && activeProfile.contains("prod");
                boolean isDevMock = !isProd && devMockAuthEnabled && token.startsWith("mock-");

                // 3. Find or auto-provision the user record in PostgreSQL
                User user = userRepository.findByFirebaseUid(tokenInfo.getUid())
                        .or(() -> tokenInfo.getEmail() != null ? userRepository.findByEmail(tokenInfo.getEmail()) : java.util.Optional.empty())
                        .map(existingUser -> {
                            // Only in explicit dev mock mode: allow mock-admin tokens to test administrative privileges
                            if (isDevMock && token.startsWith("mock-admin") && existingUser.getRole() != UserRole.SUPER_ADMIN) {
                                existingUser.setRole(UserRole.SUPER_ADMIN);
                                return userRepository.save(existingUser);
                            }
                            return existingUser;
                        })
                        .orElseGet(() -> {
                            log.info("Auto-provisioning new user for Firebase UID: {}", tokenInfo.getUid());

                            // Production rule: All newly provisioned users start strictly with USER role
                            UserRole initialRole = UserRole.USER;
                            if (isDevMock && token.startsWith("mock-admin")) {
                                initialRole = UserRole.SUPER_ADMIN;
                            }

                            User newUser = User.builder()
                                    .firebaseUid(tokenInfo.getUid())
                                    .email(tokenInfo.getEmail() != null ? tokenInfo.getEmail() : tokenInfo.getUid() + "@openmosque.org")
                                    .displayName(tokenInfo.getName() != null ? tokenInfo.getName() : "Mosque Contributor")
                                    .photoUrl(tokenInfo.getPicture())
                                    .role(initialRole)
                                    .points(0)
                                    .active(true)
                                    .verified(false)
                                    .build();
                            return userRepository.save(newUser);
                        });

                // 4. Verify account status: Block suspended or soft-deleted accounts immediately
                if (!user.isActive() || user.isDeleted()) {
                    log.warn("Blocked request from inactive or deleted user: {}", user.getEmail());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    ApiResponse<Void> errorResp = ApiResponse.error("ACCOUNT_DISABLED", "Your account has been deactivated or suspended.");
                    response.getWriter().write(objectMapper.writeValueAsString(errorResp));
                    return;
                }

                // 5. Populate Spring Security Context
                CustomUserDetails userDetails = new CustomUserDetails(user);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 6. Continue filter chain execution
        filterChain.doFilter(request, response);
    }

    /**
     * Helper method to parse 'Bearer <token>' string from Authorization header or HttpOnly cookie.
     */
    private String extractBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("om_access_token".equals(cookie.getName()) || "access_token".equals(cookie.getName())) {
                    if (StringUtils.hasText(cookie.getValue())) {
                        return cookie.getValue();
                    }
                }
            }
        }
        return null;
    }
}
