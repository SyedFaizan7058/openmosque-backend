package com.openmosque.modules.mosque.service;

import com.openmosque.common.exception.ForbiddenException;
import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.modules.claim.entity.ClaimStatus;
import com.openmosque.modules.claim.repository.MosqueClaimRequestRepository;
import com.openmosque.modules.community.entity.ContentStatus;
import com.openmosque.modules.community.entity.FlagStatus;
import com.openmosque.modules.community.entity.MosqueReview;
import com.openmosque.modules.community.entity.QuestionStatus;
import com.openmosque.modules.community.repository.CommunityContentFlagRepository;
import com.openmosque.modules.community.repository.MosqueQuestionRepository;
import com.openmosque.modules.community.repository.MosqueReviewRepository;
import com.openmosque.modules.event.repository.MosqueEventRepository;
import com.openmosque.modules.moderation.dto.PlatformStatsDto;
import com.openmosque.modules.moderation.entity.SubmissionStatus;
import com.openmosque.modules.moderation.repository.MosqueSubmissionRepository;
import com.openmosque.modules.mosque.dto.MosqueAdminStatsDto;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.mosque.repository.UserFavoriteMosqueRepository;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final MosqueRepository mosqueRepository;
    private final UserFavoriteMosqueRepository favoriteRepository;
    private final MosqueReviewRepository reviewRepository;
    private final MosqueEventRepository eventRepository;
    private final MosqueQuestionRepository questionRepository;
    private final MosqueSubmissionRepository submissionRepository;
    private final MosqueClaimRequestRepository claimRepository;
    private final CommunityContentFlagRepository flagRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MosqueAdminStatsDto getMosqueAdminStats(UUID mosqueId, User user) {
        validateAdminPermission(mosqueId, user);

        Mosque mosque = mosqueRepository.findByIdAndDeletedFalse(mosqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Mosque", "id", mosqueId));

        long totalFavorites = favoriteRepository.countByMosqueId(mosqueId);

        // Rating summary
        Object[] summary = reviewRepository.getRatingSummaryByMosqueId(mosqueId);
        long totalReviews = 0;
        double averageRating = 0.0;
        if (summary != null && summary.length > 0) {
            Object first = summary[0];
            if (first instanceof Object[]) {
                Object[] row = (Object[]) first;
                totalReviews = row[0] != null ? ((Number) row[0]).longValue() : 0L;
                averageRating = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            } else if (summary.length >= 2) {
                totalReviews = summary[0] != null ? ((Number) summary[0]).longValue() : 0L;
                averageRating = summary[1] != null ? ((Number) summary[1]).doubleValue() : 0.0;
            }
        }

        // Breakdown by rating
        Map<Integer, Long> breakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            breakdown.put(i, 0L);
        }
        List<MosqueReview> reviews = reviewRepository.findByMosqueIdAndStatusAndDeletedFalse(
                mosqueId, ContentStatus.PUBLISHED, Pageable.unpaged()).getContent();
        for (MosqueReview r : reviews) {
            int rating = Math.max(1, Math.min(5, (int) Math.round(r.getRatingOverall())));
            breakdown.put(rating, breakdown.get(rating) + 1);
        }

        long upcomingEvents = eventRepository.findUpcomingEventsByMosqueId(mosqueId, Instant.now()).size();
        long unansweredQuestions = questionRepository.countByMosqueIdAndStatusAndDeletedFalse(mosqueId, QuestionStatus.OPEN);

        return MosqueAdminStatsDto.builder()
                .totalFavorites(totalFavorites)
                .totalReviews(totalReviews)
                .averageRating(Math.round(averageRating * 10.0) / 10.0)
                .ratingsBreakdown(breakdown)
                .upcomingEventsCount(upcomingEvents)
                .unansweredQuestionsCount(unansweredQuestions)
                .build();
    }

    @Transactional(readOnly = true)
    public PlatformStatsDto getPlatformStats() {
        long totalMosques = mosqueRepository.countByDeletedFalse();
        long verifiedMosques = mosqueRepository.countByVerifiedTrueAndDeletedFalse();
        long pendingSubmissions = submissionRepository.countByStatus(SubmissionStatus.PENDING);
        long pendingClaims = claimRepository.countByStatus(ClaimStatus.PENDING);
        long activeFlags = flagRepository.countByStatus(FlagStatus.PENDING);
        long totalUsers = userRepository.countByDeletedFalse();

        return PlatformStatsDto.builder()
                .totalMosques(totalMosques)
                .verifiedMosques(verifiedMosques)
                .pendingSubmissions(pendingSubmissions)
                .pendingClaims(pendingClaims)
                .activeFlags(activeFlags)
                .totalUsers(totalUsers)
                .build();
    }

    private void validateAdminPermission(UUID mosqueId, User user) {
        if (user.getRole() == UserRole.SUPER_ADMIN || user.getRole() == UserRole.MODERATOR) {
            return;
        }
        boolean hasApprovedClaim = claimRepository.existsByMosqueIdAndClaimantIdAndStatus(
                mosqueId, user.getId(), ClaimStatus.APPROVED);
        if (!hasApprovedClaim) {
            throw new ForbiddenException("You do not have administrative permission for this mosque");
        }
    }
}
