package com.openmosque.modules.user.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.user.dto.UserResponseDto;
import com.openmosque.modules.user.dto.UserSyncRequestDto;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.service.UserService;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for User Profiles and Authentication Sync.
 * 
 * WHY THIS IS PRESENT:
 * Exposes endpoints for frontend clients to retrieve their own user profile
 * and sync newly registered Firebase accounts into PostgreSQL.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and authentication synchronization endpoints")
public class UserController {

    private final UserService userService;

    /**
     * Retrieves the profile of the currently logged-in user.
     * Uses @CurrentUser to automatically extract the authenticated user from Spring Security Context.
     * 
     * @param user Automatically resolved authenticated User entity
     * @return ApiResponse containing UserResponseDto
     */
    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile", description = "Returns the profile, role, and points of the currently logged-in user.")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(@CurrentUser User user) {
        UserResponseDto profile = userService.getCurrentUserProfile(user);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    /**
     * Updates current user's preferred location (city, country, coordinates).
     */
    @RequestMapping(value = {"/me/location", "/location"}, method = {RequestMethod.PUT, RequestMethod.PATCH})
    @Operation(summary = "Update current user preferred location", description = "Saves user's preferred city and GPS coordinates for personalized mosque listings.")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateLocation(
            @CurrentUser User user,
            @Valid @RequestBody com.openmosque.modules.user.dto.UserLocationUpdateRequestDto request
    ) {
        UserResponseDto updated = userService.updateUserLocation(user, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "User location preferences updated successfully"));
    }

    /**
     * Synchronizes Firebase authentication claims with the PostgreSQL database.
     * Called by mobile/web frontend after a user successfully logs in via Firebase.
     * 
     * @param request Validated UserSyncRequestDto payload
     * @return ApiResponse containing the synchronized UserResponseDto
     */
    @PostMapping("/sync")
    @Operation(summary = "Synchronize Firebase user credentials with database", description = "Creates or updates the user profile matching the Firebase UID.")
    public ResponseEntity<ApiResponse<UserResponseDto>> syncUser(
            @Valid @RequestBody UserSyncRequestDto request,
            jakarta.servlet.http.HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        UserResponseDto syncedUser = userService.syncUser(request);

        boolean isSecure = httpRequest.isSecure();
        org.springframework.http.ResponseCookie accessCookie = org.springframework.http.ResponseCookie.from("om_access_token", request.getFirebaseUid())
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .sameSite("Lax")
                .maxAge(java.time.Duration.ofDays(7))
                .build();
        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, accessCookie.toString());

        return ResponseEntity.ok(ApiResponse.success(syncedUser, "User synchronized successfully"));
    }
}
