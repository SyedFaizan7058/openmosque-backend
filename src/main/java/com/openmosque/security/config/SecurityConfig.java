package com.openmosque.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openmosque.common.model.ApiResponse;
import com.openmosque.security.filter.FirebaseAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 6 Configuration for OpenMosque Backend.
 * 
 * SECURITY HARDENING:
 * 1. Comprehensive HTTP Security Headers (HSTS, CSP, X-Content-Type-Options, X-Frame-Options, Referrer, Permissions).
 * 2. Strict CORS policy with credentials control.
 * 3. Stateless JWT Bearer session management.
 * 4. Defense-in-depth role authorization across public, user, moderator, mosque-admin, and super-admin routes.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final FirebaseAuthFilter firebaseAuthFilter;
    private final com.openmosque.security.ratelimit.RateLimitingFilter rateLimitingFilter;
    private final ObjectMapper objectMapper;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String allowedMethods;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF since REST APIs use stateless JWT tokens and HttpOnly cookies with SameSite
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults()) // X-Content-Type-Options: nosniff
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny) // X-Frame-Options: DENY
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)) // HSTS 1 year
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'none'; object-src 'none'; base-uri 'self'"))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicy(permissions -> permissions.policy("geolocation=(self), camera=(), microphone=()"))
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                        .accessDeniedHandler(customAccessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Documentation & Health Endpoints (Public)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // 2. Explicitly Protected User & Favorites Routes
                        .requestMatchers("/api/v1/users/me/favorites/**", "/api/v1/users/me/badges", "/api/v1/users/me/notifications/**").authenticated()
                        .requestMatchers("/api/v1/mosques/*/favorite", "/api/v1/mosques/*/is-favorite").authenticated()

                        // 3. Public Mosque Discovery, Badges Catalog, Media Files & Prayer APIs (Public)
                        .requestMatchers(HttpMethod.GET, "/api/v1/badges", "/api/v1/users/*/badges").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/mosques/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/prayer-times/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/facilities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/calculation-methods/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/media/files/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/media/upload", "/api/v1/media/mock-upload").permitAll()

                        // 4. User Authentication, Session, 2FA & Initial Registration Sync (Public)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/sync").permitAll()

                        // 5. Moderation, Claims, Community Flags, Ingestion & Stats Routes (Requires MODERATOR or SUPER_ADMIN role)
                        .requestMatchers("/api/v1/admin/moderation/**").hasAnyRole("MODERATOR", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/admin/mosques/claims/**").hasAnyRole("MODERATOR", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/admin/community/**").hasAnyRole("MODERATOR", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/admin/stats/**").hasAnyRole("MODERATOR", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/admin/ingest/**").hasAnyRole("MODERATOR", "SUPER_ADMIN")
                        
                        // 6. Super Admin Only Routes (Requires SUPER_ADMIN role)
                        .requestMatchers("/api/v1/admin/users/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("SUPER_ADMIN")

                        // 7. Mosque Admin Analytics (Inspectable by MOSQUE_ADMIN, SUPER_ADMIN, and MODERATOR)
                        .requestMatchers("/api/v1/mosque-admin/mosques/*/stats").hasAnyRole("MOSQUE_ADMIN", "SUPER_ADMIN", "MODERATOR")

                        // 8. Mosque Admin Management (Requires MOSQUE_ADMIN or SUPER_ADMIN role)
                        .requestMatchers("/api/v1/mosque-admin/**").hasAnyRole("MOSQUE_ADMIN", "SUPER_ADMIN")

                        // 9. All other API calls require an authenticated user
                        .anyRequest().authenticated()
                )
                // Insert our custom Firebase token filter before standard username/password filter
                .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Chain Token Bucket Rate Limiting Filter after authentication so authenticated identities are known
                .addFilterAfter(rateLimitingFilter, FirebaseAuthFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiResponse<Void> apiResponse = ApiResponse.error("UNAUTHORIZED", "Authentication required. Please provide a valid Bearer token.");
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        };
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiResponse<Void> apiResponse = ApiResponse.error("FORBIDDEN", "Access denied: You do not have sufficient permissions to access this resource.");
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        boolean hasWildcard = origins.contains("*");
        if (hasWildcard) {
            configuration.addAllowedOriginPattern("*");
            configuration.setAllowCredentials(false);
        } else {
            configuration.setAllowedOrigins(origins);
            configuration.setAllowCredentials(true);
        }

        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "X-Device-Token"));
        configuration.setExposedHeaders(List.of("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset", "Retry-After", "Set-Cookie"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
