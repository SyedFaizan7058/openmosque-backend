package com.openmosque.modules.moderation.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.moderation.dto.PlatformStatsDto;
import com.openmosque.modules.mosque.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN')")
@Tag(name = "Platform Analytics (Admin)", description = "Aggregated system-wide counts for Moderation and Platform Admins")
public class PlatformAnalyticsAdminController {

    private final StatsService statsService;

    @Operation(summary = "Get platform-wide statistics", description = "Retrieves system totals for mosques, verified mosques, pending queues, and registered users.")
    @GetMapping
    public ResponseEntity<ApiResponse<PlatformStatsDto>> getPlatformStats() {
        PlatformStatsDto stats = statsService.getPlatformStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
