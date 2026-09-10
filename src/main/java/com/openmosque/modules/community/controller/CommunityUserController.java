package com.openmosque.modules.community.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.community.dto.*;
import com.openmosque.modules.community.service.CommunityModerationService;
import com.openmosque.modules.community.service.CommunityQAService;
import com.openmosque.modules.community.service.CommunityReviewService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Authenticated REST Controller for worshipper contributions, reviews, and content reporting.
 *
 * WHY THIS IS WRITTEN:
 * Allows authenticated members to write reviews, edit their feedback, ask community questions,
 * post answers, and report content that violates community guidelines.
 *
 * WHERE IT IS USED:
 * - Worshipper review submission modal (/mosques/:id in frontend)
 * - Community Q&A asking & answering input boxes
 * - Content reporting flag modal on reviews and comments
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Community & Reviews (Authenticated)", description = "Endpoints for logged-in worshippers to post reviews, ask questions, write answers, and report content")
public class CommunityUserController {

    private final CommunityReviewService reviewService;
    private final CommunityQAService qaService;
    private final CommunityModerationService moderationService;

    /**
     * Submits a new review and category ratings for a mosque.
     * Enforces one review per user per mosque.
     *
     * @param mosqueId Target mosque UUID
     * @param dto Review payload containing ratings and comments
     * @param user Authenticated user submitting review
     * @return Created review DTO with HTTP 201 Created
     */
    @PostMapping("/mosques/{id}/reviews")
    @Operation(summary = "Submit a mosque review", description = "Submits a rating and text review for a mosque. Enforces one review per user per mosque.")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(
            @PathVariable("id") UUID mosqueId,
            @Valid @RequestBody ReviewCreateDto dto,
            @CurrentUser User user
    ) {
        ReviewResponseDto response = reviewService.createReview(mosqueId, dto, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Review posted successfully"));
    }

    /**
     * Updates an existing review written by the authenticated user.
     *
     * @param mosqueId Target mosque UUID
     * @param reviewId Review UUID to update
     * @param dto Updated review ratings and text
     * @param user Authenticated user
     * @return Updated review DTO
     */
    @PutMapping({"/mosques/{id}/reviews/{reviewId}", "/community/reviews/{reviewId}"})
    @Operation(summary = "Update own review", description = "Modifies existing ratings and feedback.")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> updateReview(
            @PathVariable(value = "id", required = false) UUID mosqueId,
            @PathVariable("reviewId") UUID reviewId,
            @Valid @RequestBody ReviewCreateDto dto,
            @CurrentUser User user
    ) {
        ReviewResponseDto response = (mosqueId != null)
                ? reviewService.updateReview(mosqueId, reviewId, dto, user)
                : reviewService.updateReview(reviewId, dto, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Review updated successfully"));
    }

    /**
     * Soft-deletes a review written by the authenticated user or moderated by staff.
     *
     * @param mosqueId Target mosque UUID (optional)
     * @param reviewId Review UUID to delete
     * @param user Authenticated user
     * @return Success response
     */
    @DeleteMapping({"/mosques/{id}/reviews/{reviewId}", "/community/reviews/{reviewId}"})
    @Operation(summary = "Delete review", description = "Soft-deletes a review.")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable(value = "id", required = false) UUID mosqueId,
            @PathVariable("reviewId") UUID reviewId,
            @CurrentUser User user
    ) {
        if (mosqueId != null) {
            reviewService.deleteReview(mosqueId, reviewId, user);
        } else {
            reviewService.deleteReview(reviewId, user);
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }

    /**
     * Posts a new question to the community regarding a mosque.
     *
     * @param mosqueId Target mosque UUID
     * @param dto Question payload
     * @param user Authenticated user
     * @return Created question DTO with HTTP 201 Created
     */
    @PostMapping("/mosques/{id}/questions")
    @Operation(summary = "Ask a community question", description = "Submits a question about facilities, timings, or services to the community.")
    public ResponseEntity<ApiResponse<QuestionResponseDto>> createQuestion(
            @PathVariable("id") UUID mosqueId,
            @Valid @RequestBody QuestionCreateDto dto,
            @CurrentUser User user
    ) {
        QuestionResponseDto response = qaService.createQuestion(mosqueId, dto, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Question submitted successfully"));
    }

    /**
     * Answers an existing community question.
     * Automatically assigns an official badge if answered by a MOSQUE_ADMIN or SUPER_ADMIN.
     *
     * @param questionId Target question UUID
     * @param dto Answer payload
     * @param user Authenticated user
     * @return Created answer DTO with HTTP 201 Created
     */
    @PostMapping("/community/questions/{questionId}/answers")
    @Operation(summary = "Answer a question", description = "Answers a community question. Automatically badged if posted by the mosque's Imam/Admin.")
    public ResponseEntity<ApiResponse<AnswerResponseDto>> createAnswer(
            @PathVariable("questionId") UUID questionId,
            @Valid @RequestBody AnswerCreateDto dto,
            @CurrentUser User user
    ) {
        AnswerResponseDto response = qaService.createAnswer(questionId, dto, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Answer posted successfully"));
    }

    /**
     * Updates an existing question. Only author or SUPER_ADMIN permitted.
     */
    @PutMapping("/community/questions/{questionId}")
    @Operation(summary = "Update own question", description = "Modifies existing question text.")
    public ResponseEntity<ApiResponse<QuestionResponseDto>> updateQuestion(
            @PathVariable("questionId") UUID questionId,
            @Valid @RequestBody QuestionCreateDto dto,
            @CurrentUser User user
    ) {
        QuestionResponseDto response = qaService.updateQuestion(questionId, dto, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Question updated successfully"));
    }

    /**
     * Deletes a question. Author, Moderator, Super Admin, or Mosque Admin for this mosque permitted.
     */
    @DeleteMapping("/community/questions/{questionId}")
    @Operation(summary = "Delete question", description = "Soft-deletes a question.")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable("questionId") UUID questionId,
            @CurrentUser User user
    ) {
        qaService.deleteQuestion(questionId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Question deleted successfully"));
    }

    /**
     * Updates an existing answer. Only author or SUPER_ADMIN permitted.
     */
    @PutMapping("/community/answers/{answerId}")
    @Operation(summary = "Update own answer", description = "Modifies existing answer text.")
    public ResponseEntity<ApiResponse<AnswerResponseDto>> updateAnswer(
            @PathVariable("answerId") UUID answerId,
            @Valid @RequestBody AnswerCreateDto dto,
            @CurrentUser User user
    ) {
        AnswerResponseDto response = qaService.updateAnswer(answerId, dto, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Answer updated successfully"));
    }

    /**
     * Deletes an answer. Author, Moderator, Super Admin, or Mosque Admin for this mosque permitted.
     */
    @DeleteMapping("/community/answers/{answerId}")
    @Operation(summary = "Delete answer", description = "Soft-deletes an answer.")
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(
            @PathVariable("answerId") UUID answerId,
            @CurrentUser User user
    ) {
        qaService.deleteAnswer(answerId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Answer deleted successfully"));
    }

    /**
     * Flags a piece of content (review, question, or answer) for moderator inspection.
     *
     * @param dto Flag payload containing targetType, targetId, and reason
     * @param user Authenticated reporter
     * @return Created flag DTO with HTTP 201 Created
     */
    @PostMapping("/community/flag")
    @Operation(summary = "Report / Flag content", description = "Reports inappropriate or abusive reviews, questions, or answers to community moderators.")
    public ResponseEntity<ApiResponse<ContentFlagResponseDto>> flagContent(
            @Valid @RequestBody ContentFlagCreateDto dto,
            @CurrentUser User user
    ) {
        ContentFlagResponseDto response = moderationService.flagContent(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Content report submitted for moderation review"));
    }
}
