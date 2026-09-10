package com.openmosque.modules.community.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.community.dto.QuestionResponseDto;
import com.openmosque.modules.community.dto.RatingSummaryDto;
import com.openmosque.modules.community.dto.ReviewResponseDto;
import com.openmosque.modules.community.service.CommunityQAService;
import com.openmosque.modules.community.service.CommunityReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Public REST Controller for community reviews, ratings, and Q&A interactions.
 *
 * WHY THIS IS WRITTEN:
 * Gives prospective visitors, tourists, and local worshippers immediate visibility into
 * community sentiment, cleanliness feedback, accessibility ratings, and common questions.
 *
 * WHERE IT IS USED:
 * - Mosque Profile Screen (Reviews tab, Ratings widget, and Q&A accordion in MosqueDetail.jsx)
 * - Directory Card previews (Average overall star rating in MosqueCard.jsx)
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Community & Reviews (Public)", description = "Public endpoints for mosque ratings, community reviews, and Q&A discussions")
public class CommunityPublicController {

    private final CommunityReviewService reviewService;
    private final CommunityQAService qaService;

    /**
     * Retrieves paginated community reviews for a mosque.
     *
     * @param idOrSlug Mosque UUID or unique slug
     * @param pageable Pagination and sort parameters
     * @return Paginated PageResponse of ReviewResponseDto
     */
    @GetMapping("/mosques/{idOrSlug}/reviews")
    @Operation(summary = "List mosque reviews", description = "Retrieves paginated community reviews with ratings breakdown.")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponseDto>>> getReviews(
            @PathVariable("idOrSlug") String idOrSlug,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ReviewResponseDto> page = reviewService.getReviews(idOrSlug, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page), "Reviews retrieved successfully"));
    }

    /**
     * Retrieves aggregated rating statistics across all 5 categories.
     *
     * @param id Mosque UUID
     * @return RatingSummaryDto containing total reviews count and category averages
     */
    @GetMapping({"/mosques/{idOrSlug}/ratings", "/mosques/{idOrSlug}/ratings-summary"})
    @Operation(summary = "Get aggregated ratings", description = "Retrieves rating summary averages (Cleanliness, Facilities, Women's Area, Parking, Overall).")
    public ResponseEntity<ApiResponse<RatingSummaryDto>> getRatingSummary(@PathVariable("idOrSlug") String idOrSlug) {
        RatingSummaryDto summary = reviewService.getRatingSummary(idOrSlug);
        return ResponseEntity.ok(ApiResponse.success(summary, "Rating summary retrieved successfully"));
    }

    /**
     * Retrieves community Q&A question threads with answered replies.
     *
     * @param idOrSlug Mosque UUID or unique slug
     * @param pageable Pagination parameters
     * @return Paginated PageResponse of QuestionResponseDto
     */
    @GetMapping("/mosques/{idOrSlug}/questions")
    @Operation(summary = "List community Q&A", description = "Retrieves community questions and answers with official Imam verification badges.")
    public ResponseEntity<ApiResponse<PageResponse<QuestionResponseDto>>> getQuestions(
            @PathVariable("idOrSlug") String idOrSlug,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<QuestionResponseDto> page = qaService.getQuestions(idOrSlug, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page), "Community Q&A retrieved successfully"));
    }
}
