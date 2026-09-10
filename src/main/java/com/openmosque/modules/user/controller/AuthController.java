package com.openmosque.modules.user.controller;

import com.openmosque.common.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Controller for setting and clearing HttpOnly authentication cookies.
 * 
 * WHY THIS IS PRESENT:
 * Enforces production security where access and refresh tokens are stored exclusively
 * in HttpOnly Secure cookies to prevent XSS credential theft.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Session", description = "Endpoints for HttpOnly cookie session management")
public class AuthController {

    @Data
    public static class SessionRequest {
        private String token;
        private String refreshToken;
    }

    @Operation(summary = "Establish HttpOnly cookie session", description = "Sets om_access_token and om_refresh_token in HttpOnly cookies.")
    @PostMapping("/session")
    public ResponseEntity<ApiResponse<String>> establishSession(
            @RequestBody(required = false) SessionRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        String token = null;
        if (request != null && StringUtils.hasText(request.getToken())) {
            token = request.getToken();
        } else if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (!StringUtils.hasText(token)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("BAD_REQUEST", "Token must be provided in body or Authorization header"));
        }

        boolean isSecure = httpRequest.isSecure();

        ResponseCookie accessCookie = ResponseCookie.from("om_access_token", token)
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(7))
                .build();

        String refreshToken = (request != null && StringUtils.hasText(request.getRefreshToken()))
                ? request.getRefreshToken()
                : token + "-refresh";

        ResponseCookie refreshCookie = ResponseCookie.from("om_refresh_token", refreshToken)
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(30))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(ApiResponse.success("Session established", "HttpOnly cookies configured successfully"));
    }

    @Operation(summary = "Clear HttpOnly cookie session", description = "Clears om_access_token and om_refresh_token cookies.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> clearSession(
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        boolean isSecure = httpRequest.isSecure();

        ResponseCookie accessCookie = ResponseCookie.from("om_access_token", "")
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("om_refresh_token", "")
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(ApiResponse.success("Logged out", "Session cookies cleared"));
    }
}
