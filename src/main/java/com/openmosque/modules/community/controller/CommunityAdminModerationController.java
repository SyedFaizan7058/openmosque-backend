package com.openmosque.modules.community.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.community.dto.ContentFlagResponseDto;
import com.openmosque.modules.community.dto.FlagDecisionDto;
import com.openmosque.modules.community.service.CommunityModerationService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Administrative REST Controller for Community Moderators and Super Admins.
 *
 * WHY THIS IS WRITTEN:
 * Empowers trusted platform moderators with a dedicated review queue to audit community reports
 * against violating reviews, spam questions, and abusive answers. Confirmed violations automatically
 * hide content from public directory search results.
 *
 * WHERE IT IS USED:
 * - Moderator Dashboard (Flagged Content tab in /admin and /moderator screens)
 */
@RestController
@RequestMapping("/api/v1/admin/community")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN')")
@Tag(name = "Moderation (Community Flags)", description = "Review queue for moderators to inspect reported community reviews, questions, and answers")
public class CommunityAdminModerationController {

    private final CommunityModerationService moderationService;

    /**
     * Lists all pending content flags requiring review.
     *
     * @param pageable Pagination and sort parameters
     * @return Paginated PageResponse of ContentFlagResponseDto
     */
    @GetMapping("/flags")
    @Operation(summary = "List pending content flags", description = "Retrieves queue of reported community content requiring moderation action.")
    public ResponseEntity<ApiResponse<PageResponse<ContentFlagResponseDto>>> getPendingFlags(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ContentFlagResponseDto> page = moderationService.getPendingFlags(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page), "Pending flags retrieved successfully"));
    }

    /**
     * Issues an audit decision on a reported content flag.
     * Selecting RESOLVED automatically hides the target review, question, or answer from public listings.
     *
     * @param flagId Target flag UUID
     * @param decision Decision payload (RESOLVED or DISMISSED + audit notes)
     * @param reviewer Authenticated moderator or super admin
     * @return Updated flag DTO
     */
    @PatchMapping("/flags/{id}/decision")
    @Operation(summary = "Decide on content flag", description = "Resolves (hides violating content) or dismisses reported content flags.")
    public ResponseEntity<ApiResponse<ContentFlagResponseDto>> decideFlag(
            @PathVariable("id") UUID flagId,
            @Valid @RequestBody FlagDecisionDto decision,
            @CurrentUser User reviewer
    ) {
        ContentFlagResponseDto response = moderationService.decideFlag(flagId, decision, reviewer);
        return ResponseEntity.ok(ApiResponse.success(response, "Content flag decided successfully"));
    }
}
