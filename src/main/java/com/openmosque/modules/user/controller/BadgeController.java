package com.openmosque.modules.user.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.user.dto.BadgeDto;
import com.openmosque.modules.user.dto.UserBadgeResponseDto;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.service.BadgeService;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Badges & Gamification", description = "Endpoints for platform badges, achievements, and user awards")
public class BadgeController {

    private final BadgeService badgeService;

    @Operation(summary = "Get all platform badges", description = "Retrieves catalog of all active badges and award criteria.")
    @GetMapping("/badges")
    public ResponseEntity<ApiResponse<List<BadgeDto>>> getAllBadges() {
        return ResponseEntity.ok(ApiResponse.success(badgeService.getAllActiveBadges()));
    }

    @Operation(summary = "Get current user's badges", description = "Retrieves all badges earned by the authenticated user.")
    @GetMapping("/users/me/badges")
    public ResponseEntity<ApiResponse<List<UserBadgeResponseDto>>> getMyBadges(@CurrentUser User user) {
        return ResponseEntity.ok(ApiResponse.success(badgeService.getUserBadges(user.getId())));
    }

    @Operation(summary = "Get user public badges", description = "Retrieves earned badges for a specific user profile.")
    @GetMapping("/users/{id}/badges")
    public ResponseEntity<ApiResponse<List<UserBadgeResponseDto>>> getUserBadges(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(badgeService.getUserBadges(id)));
    }
}
