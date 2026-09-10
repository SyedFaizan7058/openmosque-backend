package com.openmosque.modules.moderation.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.moderation.dto.MosqueSubmissionRequestDto;
import com.openmosque.modules.moderation.dto.MosqueSubmissionResponseDto;
import com.openmosque.modules.moderation.entity.SubmissionType;
import com.openmosque.modules.moderation.service.ModerationService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for Crowdsourced User Submissions and Mosque Edit Suggestions.
 */
@RestController
@RequestMapping("/api/v1/mosques")
@RequiredArgsConstructor
@Tag(name = "Crowdsourcing & Submissions", description = "Endpoints for users to contribute new mosques and suggest corrections")
public class MosqueContributionController {

    private final ModerationService moderationService;

    /**
     * Submit a new mosque for moderator review.
     */
    @PostMapping("/submissions")
    @Operation(summary = "Submit a new mosque proposal", description = "Submits a new mosque into the moderation review queue. Users earn reward points upon approval.")
    public ResponseEntity<ApiResponse<MosqueSubmissionResponseDto>> submitNewMosque(
            @Valid @RequestBody MosqueSubmissionRequestDto request,
            @CurrentUser User submitter
    ) {
        request.setSubmissionType(SubmissionType.NEW_MOSQUE);
        request.setTargetMosqueId(null);
        MosqueSubmissionResponseDto submission = moderationService.submitContribution(request, submitter);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(submission, "Mosque proposal submitted for review"));
    }

    /**
     * Suggest edits / corrections to an existing mosque profile.
     */
    @PostMapping("/{id}/suggest-edit")
    @Operation(summary = "Suggest corrections to existing mosque", description = "Proposes updated timings, facilities, or contacts for an existing mosque.")
    public ResponseEntity<ApiResponse<MosqueSubmissionResponseDto>> suggestEdit(
            @PathVariable("id") UUID mosqueId,
            @Valid @RequestBody MosqueSubmissionRequestDto request,
            @CurrentUser User submitter
    ) {
        request.setSubmissionType(SubmissionType.EDIT_SUGGESTION);
        request.setTargetMosqueId(mosqueId);
        MosqueSubmissionResponseDto submission = moderationService.submitContribution(request, submitter);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(submission, "Edit suggestion submitted for review"));
    }
}
