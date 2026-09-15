package com.openmosque.modules.user.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.user.dto.UserResponseDto;
import com.openmosque.modules.user.dto.UserRoleUpdateRequestDto;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Super Admin User & Role Management Controller.
 * 
 * WHY THIS IS PRESENT:
 * Fulfills Way A: Allows Super Admins to promote or demote any user to MODERATOR, MOSQUE_ADMIN, SUPER_ADMIN, or USER.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "User Management (Super Admin)", description = "Endpoints for Super Admins to change user roles and view registered accounts")
public class UserAdminController {

    private final UserService userService;

    /**
     * Updates any user's role (Super Admin only).
     */
    @PatchMapping("/{id}/role")
    @Operation(summary = "Update user role", description = "Promotes or demotes a user's system authorization role (USER, MOSQUE_ADMIN, MODERATOR, SUPER_ADMIN).")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserRole(
            @PathVariable("id") UUID userId,
            @Valid @RequestBody UserRoleUpdateRequestDto request
    ) {
        UserResponseDto updatedUser = userService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User role updated successfully"));
    }

    /**
     * List all users with pagination, role filter, and search query.
     */
    @GetMapping
    @Operation(summary = "List users", description = "Retrieves paginated list of users with optional role filtering and search query.")
    public ResponseEntity<ApiResponse<PageResponse<UserResponseDto>>> listUsers(
            @Parameter(description = "Optional filter by role")
            @RequestParam(value = "role", required = false) UserRole role,

            @Parameter(description = "Optional search query for email or name")
            @RequestParam(value = "search", required = false) String search,

            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<UserResponseDto> users = userService.listUsers(role, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
}
