package com.openmosque.modules.user.service;

import com.openmosque.modules.user.dto.BadgeDto;
import com.openmosque.modules.user.dto.UserBadgeResponseDto;
import com.openmosque.modules.user.entity.Badge;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserBadge;
import com.openmosque.modules.user.repository.BadgeRepository;
import com.openmosque.modules.user.repository.UserBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Centralized business logic engine for badges and achievements.
 * Enforces strict idempotency and concurrency protection against duplicate awards.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {

    public static final String BADGE_PIONEER = "PIONEER";
    public static final String BADGE_MOSQUE_EXPLORER = "MOSQUE_EXPLORER";
    public static final String BADGE_CENTURION_CONTRIBUTOR = "CENTURION_CONTRIBUTOR";
    public static final String BADGE_COMMUNITY_PILLAR = "COMMUNITY_PILLAR";
    public static final String BADGE_VERIFIED_IMAM = "VERIFIED_IMAM";
    public static final String BADGE_DEVOTED_PATRON = "DEVOTED_PATRON";

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final com.openmosque.modules.notification.service.NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<BadgeDto> getAllActiveBadges() {
        return badgeRepository.findByActiveTrueOrderByCreatedAtAsc()
                .stream()
                .map(this::toBadgeDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserBadgeResponseDto> getUserBadges(UUID userId) {
        return userBadgeRepository.findAllByUserIdWithBadge(userId)
                .stream()
                .map(this::toUserBadgeDto)
                .collect(Collectors.toList());
    }

    /**
     * Idempotently awards a badge by its unique code.
     * Uses both application-level existence check and database constraint recovery.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean awardBadge(User user, String badgeCode) {
        if (user == null || badgeCode == null) {
            return false;
        }

        Optional<Badge> badgeOpt = badgeRepository.findByCodeAndActiveTrue(badgeCode);
        if (badgeOpt.isEmpty()) {
            log.warn("Cannot award badge '{}': badge does not exist or is inactive", badgeCode);
            return false;
        }

        Badge badge = badgeOpt.get();

        // 1. Application-level check
        if (userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
            log.debug("User '{}' already has badge '{}'. Skipping.", user.getEmail(), badgeCode);
            return false;
        }

        // 2. Database persistence with concurrency protection
        try {
            UserBadge userBadge = UserBadge.builder()
                    .user(user)
                    .badge(badge)
                    .build();
            userBadgeRepository.saveAndFlush(userBadge);
            log.info("Successfully awarded badge '{}' to user '{}' (User ID: {})",
                    badgeCode, user.getEmail(), user.getId());

            notificationService.notifyUser(
                    user,
                    "New Badge Unlocked: " + badge.getName(),
                    "Congratulations! You've unlocked the '" + badge.getName() + "' badge: " + badge.getDescription(),
                    com.openmosque.modules.notification.entity.NotificationType.BADGE_EARNED,
                    "/profile",
                    null
            );

            return true;
        } catch (DataIntegrityViolationException ex) {
            log.debug("Duplicate badge '{}' caught by database constraint for user '{}'. Handled safely.",
                    badgeCode, user.getEmail());
            return false;
        }
    }

    /**
     * Evaluates badges earned from bookmarking mosques.
     * - DEVOTED_PATRON: >= 1 favorite
     * - MOSQUE_EXPLORER: >= 5 favorites
     */
    @Transactional
    public void evaluateFavoritesBadges(User user, long favoriteCount) {
        if (user == null) return;

        if (favoriteCount >= 1) {
            awardBadge(user, BADGE_DEVOTED_PATRON);
        }
        if (favoriteCount >= 5) {
            awardBadge(user, BADGE_MOSQUE_EXPLORER);
        }
    }

    /**
     * Evaluates badges earned from user community points thresholds.
     * - CENTURION_CONTRIBUTOR: >= 100 points
     * - COMMUNITY_PILLAR: >= 500 points
     */
    @Transactional
    public void evaluatePointsBadges(User user) {
        if (user == null) return;

        int points = user.getPoints();
        if (points >= 100) {
            awardBadge(user, BADGE_CENTURION_CONTRIBUTOR);
        }
        if (points >= 500) {
            awardBadge(user, BADGE_COMMUNITY_PILLAR);
        }
    }

    private BadgeDto toBadgeDto(Badge b) {
        return BadgeDto.builder()
                .id(b.getId())
                .code(b.getCode())
                .name(b.getName())
                .description(b.getDescription())
                .iconName(b.getIconName())
                .category(b.getCategory())
                .thresholdPoints(b.getThresholdPoints())
                .active(b.isActive())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private UserBadgeResponseDto toUserBadgeDto(UserBadge ub) {
        Badge b = ub.getBadge();
        return UserBadgeResponseDto.builder()
                .id(ub.getId())
                .badgeId(b.getId())
                .code(b.getCode())
                .name(b.getName())
                .description(b.getDescription())
                .iconName(b.getIconName())
                .category(b.getCategory())
                .earnedAt(ub.getEarnedAt())
                .build();
    }
}
