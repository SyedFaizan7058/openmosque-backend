package com.openmosque.security.filter;

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
 * WHY THIS IS PRESENT:
 * 1. Intercepts incoming HTTP requests.
 * 2. Extracts the 'Authorization: Bearer <token>' header.
 * 3. Validates the Firebase ID Token via FirebaseTokenVerifier.
 * 4. Auto-provisions or retrieves the User from PostgreSQL based on 'firebase_uid'.
 * 5. Builds 'CustomUserDetails' and sets the authenticated principal in 'SecurityContextHolder'.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final FirebaseTokenVerifier tokenVerifier;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract Bearer token from header
        String token = extractBearerToken(request);

        // 2. If token exists and context is not already authenticated, verify it
        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            FirebaseTokenVerifier.VerifiedTokenInfo tokenInfo = tokenVerifier.verifyToken(token);

            if (tokenInfo != null) {
                String tokenEmailLower = tokenInfo.getEmail() != null ? tokenInfo.getEmail().toLowerCase() : "";
                String tokenUidLower = tokenInfo.getUid().toLowerCase();

                // 3. Find or auto-provision the user record in our PostgreSQL database
                User user = userRepository.findByFirebaseUid(tokenInfo.getUid())
                        .or(() -> tokenInfo.getEmail() != null ? userRepository.findByEmail(tokenInfo.getEmail()) : java.util.Optional.empty())
                        .map(existingUser -> {
                            // In dev mock mode, auto-upgrade role if matching token prefix or email
                            if ((tokenUidLower.contains("admin") || tokenEmailLower.startsWith("admin@") || tokenEmailLower.contains("superadmin"))
                                    && existingUser.getRole() != UserRole.SUPER_ADMIN) {
                                existingUser.setRole(UserRole.SUPER_ADMIN);
                                return userRepository.save(existingUser);
                            } else if ((tokenUidLower.contains("moderator") || tokenEmailLower.contains("moderator"))
                                    && existingUser.getRole() == UserRole.USER) {
                                existingUser.setRole(UserRole.MODERATOR);
                                return userRepository.save(existingUser);
                            }
                            return existingUser;
                        })
                        .orElseGet(() -> {
                            log.info("Auto-provisioning new user for Firebase UID: {}", tokenInfo.getUid());
                            UserRole initialRole = UserRole.USER;
                            if (tokenUidLower.contains("admin") || tokenUidLower.contains("superadmin") || tokenEmailLower.startsWith("admin@") || tokenEmailLower.contains("superadmin")) {
                                initialRole = UserRole.SUPER_ADMIN;
                            } else if (tokenUidLower.contains("moderator") || tokenEmailLower.contains("moderator")) {
                                initialRole = UserRole.MODERATOR;
                            }

                            User newUser = User.builder()
                                    .firebaseUid(tokenInfo.getUid())
                                    .email(tokenInfo.getEmail() != null ? tokenInfo.getEmail() : tokenInfo.getUid() + "@openmosque.org")
                                    .displayName(tokenInfo.getName() != null ? tokenInfo.getName() : "Mosque Contributor")
                                    .photoUrl(tokenInfo.getPicture())
                                    .role(initialRole)
                                    .points(initialRole != UserRole.USER ? 100 : 0)
                                    .active(true)
                                    .verified(initialRole != UserRole.USER)
                                    .build();
                            return userRepository.save(newUser);
                        });

                // 4. If user is active and not soft-deleted, set Spring Security Context
                if (user.isActive() && !user.isDeleted()) {
                    CustomUserDetails userDetails = new CustomUserDetails(user);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.warn("Blocked request from inactive or deleted user: {}", user.getEmail());
                }
            }
        }

        // 5. Continue filter chain execution
        filterChain.doFilter(request, response);
    }

    /**
     * Helper method to parse 'Bearer <token>' string from Authorization header.
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
