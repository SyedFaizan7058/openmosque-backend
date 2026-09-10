package com.openmosque.modules.community.service;

import com.openmosque.common.exception.ConflictException;
import com.openmosque.common.exception.ForbiddenException;
import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.common.util.SecurityUtils;
import com.openmosque.modules.community.dto.RatingSummaryDto;
import com.openmosque.modules.community.dto.ReviewCreateDto;
import com.openmosque.modules.community.dto.ReviewResponseDto;
import com.openmosque.modules.community.entity.ContentStatus;
import com.openmosque.modules.community.entity.MosqueReview;
import com.openmosque.modules.community.mapper.CommunityMapper;
import com.openmosque.modules.community.repository.MosqueReviewRepository;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.service.MosqueService;
import com.openmosque.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service managing community reviews, ratings breakdown, and worshipper feedback.
 *
 * WHY THIS IS WRITTEN:
 * Allows worshippers to share genuine reviews and transparent multi-category star ratings
 * (Overall, Cleanliness, Facilities, Women's Area, and Parking). Enforces a 1-review-per-worshipper
 * policy per mosque, and calculates live aggregated rating summaries for directory sorting.
 *
 * WHERE IT IS USED:
 * - CommunityPublicController: Public review listings & rating averages (/api/v1/mosques/{idOrSlug}/reviews)
 * - CommunityUserController: Authenticated worshipper review actions (/api/v1/mosques/{id}/reviews)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityReviewService {

    private final MosqueReviewRepository reviewRepository;
    private final MosqueService mosqueService;
    private final CommunityMapper communityMapper;
    private final ProfanityFilterService profanityFilterService;
    private final com.openmosque.modules.claim.repository.MosqueClaimRequestRepository claimRepository;

    /**
     * Retrieves paginated published reviews for a mosque.
     *
     * @param idOrSlug Mosque UUID or unique slug
     * @param pageable Pagination parameters
     * @return Paginated list of active reviews
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviews(String idOrSlug, Pageable pageable) {
        Mosque mosque = mosqueService.findEntityByIdOrSlug(idOrSlug);
        return reviewRepository.findByMosqueIdAndStatusAndDeletedFalse(mosque.getId(), ContentStatus.PUBLISHED, pageable)
                .map(communityMapper::toDto);
    }

    /**
     * Calculates aggregated rating averages across all 5 categories for a mosque.
     *
     * @param mosqueId Target mosque UUID
     * @return Aggregated RatingSummaryDto with 1-decimal rounded scores
     */
    @Transactional(readOnly = true)
    public RatingSummaryDto getRatingSummary(String idOrSlug) {
        Mosque mosque = mosqueService.findEntityByIdOrSlug(idOrSlug);
        return getRatingSummary(mosque.getId());
    }

    @Transactional(readOnly = true)
    public RatingSummaryDto getRatingSummary(UUID mosqueId) {
        Object[] row = reviewRepository.getRatingSummaryByMosqueId(mosqueId);
        if (row == null || row.length == 0 || row[0] == null) {
            return RatingSummaryDto.builder()
                    .totalReviews(0L)
                    .averageOverall(0.0)
                    .build();
        }

        Object[] cols = (row[0] instanceof Object[]) ? (Object[]) row[0] : row;
        long total = (cols[0] != null) ? ((Number) cols[0]).longValue() : 0L;
        double overall = (cols[1] != null) ? ((Number) cols[1]).doubleValue() : 0.0;
        Double cleanliness = (cols[2] != null) ? ((Number) cols[2]).doubleValue() : null;
        Double facilities = (cols[3] != null) ? ((Number) cols[3]).doubleValue() : null;
        Double womensArea = (cols[4] != null) ? ((Number) cols[4]).doubleValue() : null;
        Double parking = (cols[5] != null) ? ((Number) cols[5]).doubleValue() : null;

        return RatingSummaryDto.builder()
                .totalReviews(total)
                .averageOverall(Math.round(overall * 10.0) / 10.0)
                .averageCleanliness(cleanliness != null ? Math.round(cleanliness * 10.0) / 10.0 : null)
                .averageFacilities(facilities != null ? Math.round(facilities * 10.0) / 10.0 : null)
                .averageWomensArea(womensArea != null ? Math.round(womensArea * 10.0) / 10.0 : null)
                .averageParking(parking != null ? Math.round(parking * 10.0) / 10.0 : null)
                .build();
    }

    /**
     * Submits a new review and category ratings for a mosque.
     * Enforces one review per user per mosque. Scans text for toxic profanity.
     *
     * @param mosqueId Target mosque UUID
     * @param dto Review payload containing ratings and comments
     * @param user Authenticated user submitting the review
     * @return Created review DTO
     * @throws ConflictException if the user has already reviewed this mosque
     */
    @Transactional
    public ReviewResponseDto createReview(UUID mosqueId, ReviewCreateDto dto, User user) {
        profanityFilterService.validateCleanContent(dto.getReviewText(), "reviewText");
        Mosque mosque = mosqueService.findEntityByIdOrSlug(mosqueId.toString());

        if (reviewRepository.findByMosqueIdAndUserIdAndDeletedFalse(mosqueId, user.getId()).isPresent()) {
            throw new ConflictException("You have already submitted a review for this mosque. You can edit your existing review.");
        }

        MosqueReview review = MosqueReview.builder()
                .mosque(mosque)
                .user(user)
                .ratingOverall(dto.getRatingOverall())
                .ratingCleanliness(dto.getRatingCleanliness())
                .ratingFacilities(dto.getRatingFacilities())
                .ratingWomensArea(dto.getRatingWomensArea())
                .ratingParking(dto.getRatingParking())
                .reviewText(dto.getReviewText())
                .status(ContentStatus.PUBLISHED)
                .build();

        MosqueReview saved = reviewRepository.save(review);
        log.info("New review posted for mosque '{}' by user '{}' (rating: {})", mosque.getName(), user.getEmail(), dto.getRatingOverall());
        return communityMapper.toDto(saved);
    }

    /**
     * Updates an existing review.
     * Only the original author or a SUPER_ADMIN is permitted to update.
     *
     * @param mosqueId Target mosque UUID
     * @param reviewId Review UUID to update
     * @param dto Updated review ratings and text
     * @param user Authenticated user
     * @return Updated review DTO
     */
    @Transactional
    public ReviewResponseDto updateReview(UUID mosqueId, UUID reviewId, ReviewCreateDto dto, User user) {
        profanityFilterService.validateCleanContent(dto.getReviewText(), "reviewText");

        MosqueReview review = reviewRepository.findById(reviewId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueReview", "id", reviewId));

        if (!review.getMosque().getId().equals(mosqueId)) {
            throw new ForbiddenException("Review does not belong to the specified mosque.");
        }

        SecurityUtils.requireOwnerOrSuperAdmin(user, review.getUser().getId(), "review");

        review.setRatingOverall(dto.getRatingOverall());
        review.setRatingCleanliness(dto.getRatingCleanliness());
        review.setRatingFacilities(dto.getRatingFacilities());
        review.setRatingWomensArea(dto.getRatingWomensArea());
        review.setRatingParking(dto.getRatingParking());
        review.setReviewText(dto.getReviewText());

        MosqueReview updated = reviewRepository.save(review);
        log.info("Updated review '{}' by user '{}'", reviewId, user.getEmail());
        return communityMapper.toDto(updated);
    }

    /**
     * Soft-deletes a review.
     * Only the original author or a SUPER_ADMIN is permitted to delete.
     *
     * @param mosqueId Target mosque UUID
     * @param reviewId Review UUID to delete
     * @param user Authenticated user
     */
    @Transactional
    public void deleteReview(UUID mosqueId, UUID reviewId, User user) {
        MosqueReview review = reviewRepository.findById(reviewId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueReview", "id", reviewId));

        if (!review.getMosque().getId().equals(mosqueId)) {
            throw new ForbiddenException("Review does not belong to the specified mosque.");
        }

        boolean isMosqueAdminForThisMosque = false;
        if (user.getRole() == com.openmosque.modules.user.entity.UserRole.MOSQUE_ADMIN) {
            isMosqueAdminForThisMosque = claimRepository.findByMosqueIdAndClaimantIdAndStatus(
                    mosqueId, user.getId(), com.openmosque.modules.claim.entity.ClaimStatus.APPROVED).isPresent();
        }

        SecurityUtils.requireDeletePermission(user, review.getUser().getId(), isMosqueAdminForThisMosque, "review");

        review.setDeleted(true);
        reviewRepository.save(review);
        log.info("Soft-deleted review '{}' by user '{}'", reviewId, user.getEmail());
    }

    /**
     * Updates an existing review by reviewId directly.
     */
    @Transactional
    public ReviewResponseDto updateReview(UUID reviewId, ReviewCreateDto dto, User user) {
        profanityFilterService.validateCleanContent(dto.getReviewText(), "reviewText");

        MosqueReview review = reviewRepository.findById(reviewId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueReview", "id", reviewId));

        SecurityUtils.requireOwnerOrSuperAdmin(user, review.getUser().getId(), "review");

        review.setRatingOverall(dto.getRatingOverall());
        review.setRatingCleanliness(dto.getRatingCleanliness());
        review.setRatingFacilities(dto.getRatingFacilities());
        review.setRatingWomensArea(dto.getRatingWomensArea());
        review.setRatingParking(dto.getRatingParking());
        review.setReviewText(dto.getReviewText());

        MosqueReview updated = reviewRepository.save(review);
        log.info("Updated review '{}' by user '{}'", reviewId, user.getEmail());
        return communityMapper.toDto(updated);
    }

    /**
     * Soft-deletes a review by reviewId directly.
     */
    @Transactional
    public void deleteReview(UUID reviewId, User user) {
        MosqueReview review = reviewRepository.findById(reviewId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueReview", "id", reviewId));

        UUID mosqueId = review.getMosque().getId();
        boolean isMosqueAdminForThisMosque = false;
        if (user.getRole() == com.openmosque.modules.user.entity.UserRole.MOSQUE_ADMIN) {
            isMosqueAdminForThisMosque = claimRepository.findByMosqueIdAndClaimantIdAndStatus(
                    mosqueId, user.getId(), com.openmosque.modules.claim.entity.ClaimStatus.APPROVED).isPresent();
        }

        SecurityUtils.requireDeletePermission(user, review.getUser().getId(), isMosqueAdminForThisMosque, "review");

        review.setDeleted(true);
        reviewRepository.save(review);
        log.info("Soft-deleted review '{}' by user '{}'", reviewId, user.getEmail());
    }
}
