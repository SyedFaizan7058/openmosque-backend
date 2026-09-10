package com.openmosque.modules.mosque.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.mosque.dto.MosqueAdminStatsDto;
import com.openmosque.modules.mosque.service.StatsService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mosque-admin/mosques")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MOSQUE_ADMIN', 'SUPER_ADMIN', 'MODERATOR')")
@Tag(name = "Mosque Admin Analytics", description = "Aggregated management statistics for verified Mosque Administrators")
public class MosqueAdminAnalyticsController {

    private final StatsService statsService;

    @Operation(summary = "Get mosque administration stats", description = "Retrieves real-time counts for favorites, reviews, average rating breakdown, events, and unanswered questions.")
    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<MosqueAdminStatsDto>> getStats(
            @PathVariable UUID id,
            @CurrentUser User user
    ) {
        MosqueAdminStatsDto stats = statsService.getMosqueAdminStats(id, user);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
