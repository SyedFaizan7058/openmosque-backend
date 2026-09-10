package com.openmosque.modules.moderation.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.moderation.dto.MosqueSubmissionResponseDto;
import com.openmosque.modules.moderation.dto.SubmissionDecisionDto;
import com.openmosque.modules.moderation.entity.SubmissionStatus;
import com.openmosque.modules.moderation.service.ModerationService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
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
 * REST Controller for Moderators and Administrators to review crowdsourced contributions.
 */
@RestController
@RequestMapping("/api/v1/admin/moderation")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN')")
@Tag(name = "Moderation Queue (Admin)", description = "Endpoints for Moderators and Admins to review and approve crowdsourced mosque submissions")
public class ModerationAdminController {

    private final ModerationService moderationService;

    /**
     * List submissions in the moderation queue.
     */
    @GetMapping("/submissions")
    @Operation(summary = "Get submissions queue", description = "Retrieves submissions by status (default: PENDING). Requires MODERATOR or SUPER_ADMIN role.")
    public ResponseEntity<ApiResponse<PageResponse<MosqueSubmissionResponseDto>>> getSubmissions(
            @Parameter(description = "Submission Status (PENDING, APPROVED, REJECTED)")
            @RequestParam(value = "status", defaultValue = "PENDING") SubmissionStatus status,

            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<MosqueSubmissionResponseDto> response = moderationService.getSubmissionsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Submissions queue retrieved successfully"));
    }

    /**
     * Approve or reject a mosque submission.
     */
    @PatchMapping("/submissions/{id}/decision")
    @Operation(summary = "Approve or Reject a submission", description = "Makes a decision on a submission. On approval, creates the live mosque entry and rewards points.")
    public ResponseEntity<ApiResponse<MosqueSubmissionResponseDto>> reviewSubmission(
            @PathVariable("id") UUID submissionId,
            @Valid @RequestBody SubmissionDecisionDto decision,
            @CurrentUser User moderator
    ) {
        MosqueSubmissionResponseDto result = moderationService.reviewSubmission(submissionId, decision, moderator);
        return ResponseEntity.ok(ApiResponse.success(result, "Submission decision recorded successfully"));
    }
}
