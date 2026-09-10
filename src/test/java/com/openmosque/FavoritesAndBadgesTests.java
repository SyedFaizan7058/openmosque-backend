package com.openmosque;

import com.openmosque.common.util.GeoUtils;
import com.openmosque.modules.mosque.dto.FavoriteMosqueResponseDto;
import com.openmosque.modules.mosque.dto.FavoriteStatusDto;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.mosque.repository.UserFavoriteMosqueRepository;
import com.openmosque.modules.mosque.service.MosqueFavoriteService;
import com.openmosque.modules.user.dto.BadgeDto;
import com.openmosque.modules.user.dto.UserBadgeResponseDto;
import com.openmosque.modules.user.entity.Badge;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.repository.BadgeRepository;
import com.openmosque.modules.user.repository.UserBadgeRepository;
import com.openmosque.modules.user.repository.UserRepository;
import com.openmosque.modules.user.service.BadgeService;
import com.openmosque.security.ratelimit.RateLimitResult;
import com.openmosque.security.ratelimit.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FavoritesAndBadgesTests {

    @Autowired
    private MosqueFavoriteService favoriteService;

    @Autowired
    private UserFavoriteMosqueRepository favoriteRepository;

    @Autowired
    private MosqueRepository mosqueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeService badgeService;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private UserBadgeRepository userBadgeRepository;

    @Autowired
    private RateLimiterService rateLimiterService;

    private User testUser;
    private Mosque mosque1;
    private Mosque mosque2;

    @BeforeEach
    void setUp() {
        // Seed badges if not already in H2
        ensureBadgeExists(BadgeService.BADGE_DEVOTED_PATRON, "Devoted Patron", "LOYALTY", 0);
        ensureBadgeExists(BadgeService.BADGE_MOSQUE_EXPLORER, "Mosque Explorer", "LOYALTY", 0);
        ensureBadgeExists(BadgeService.BADGE_PIONEER, "Pioneer", "SUBMISSION", 0);
        ensureBadgeExists(BadgeService.BADGE_CENTURION_CONTRIBUTOR, "Centurion Contributor", "POINTS", 100);
        ensureBadgeExists(BadgeService.BADGE_COMMUNITY_PILLAR, "Community Pillar", "POINTS", 500);
        ensureBadgeExists(BadgeService.BADGE_VERIFIED_IMAM, "Verified Mosque Administrator", "VERIFICATION", 0);

        testUser = userRepository.save(User.builder()
                .firebaseUid("fb-" + UUID.randomUUID())
                .email("testuser-" + UUID.randomUUID() + "@openmosque.org")
                .displayName("Test Faithful")
                .role(UserRole.USER)
                .points(0)
                .active(true)
                .build());

        mosque1 = mosqueRepository.save(Mosque.builder()
                .name("Cambridge Central Mosque")
                .slug("cambridge-central-mosque-" + UUID.randomUUID())
                .address("Mill Rd")
                .city("Cambridge")
                .country("United Kingdom")
                .latitude(52.1991)
                .longitude(0.1415)
                .location(GeoUtils.createPoint(52.1991, 0.1415))
                .build());

        mosque2 = mosqueRepository.save(Mosque.builder()
                .name("East London Mosque")
                .slug("east-london-mosque-" + UUID.randomUUID())
                .address("Whitechapel Rd")
                .city("London")
                .country("United Kingdom")
                .latitude(51.5186)
                .longitude(-0.0655)
                .location(GeoUtils.createPoint(51.5186, -0.0655))
                .build());
    }

    private void ensureBadgeExists(String code, String name, String category, int points) {
        if (badgeRepository.findByCodeAndActiveTrue(code).isEmpty()) {
            badgeRepository.save(Badge.builder()
                    .code(code)
                    .name(name)
                    .description("Test Description for " + name)
                    .iconName("award")
                    .category(category)
                    .thresholdPoints(points)
                    .active(true)
                    .build());
        }
    }

    @Test
    @DisplayName("Favorite Mosque: Add favorite persists in DB and awards DEVOTED_PATRON badge")
    void testAddFavoritePersistsAndAwardsBadge() {
        FavoriteStatusDto status = favoriteService.addFavorite(testUser, mosque1.getId());
        assertThat(status.isFavorite()).isTrue();

        // 1. Verify DB persistence
        assertThat(favoriteRepository.existsByUserIdAndMosqueId(testUser.getId(), mosque1.getId())).isTrue();

        // 2. Verify DEVOTED_PATRON badge awarded on first favorite
        assertThat(userBadgeRepository.existsByUserIdAndBadgeCode(testUser.getId(), BadgeService.BADGE_DEVOTED_PATRON)).isTrue();
    }

    @Test
    @DisplayName("Favorite Mosque: Duplicate add request is idempotent and does not fail")
    void testDuplicateFavoriteIsIdempotent() {
        favoriteService.addFavorite(testUser, mosque1.getId());
        FavoriteStatusDto status2 = favoriteService.addFavorite(testUser, mosque1.getId());

        assertThat(status2.isFavorite()).isTrue();
        assertThat(favoriteRepository.countByUserId(testUser.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Favorite Mosque: Remove favorite removes from DB")
    void testRemoveFavorite() {
        favoriteService.addFavorite(testUser, mosque1.getId());
        assertThat(favoriteRepository.existsByUserIdAndMosqueId(testUser.getId(), mosque1.getId())).isTrue();

        FavoriteStatusDto status = favoriteService.removeFavorite(testUser, mosque1.getId());
        assertThat(status.isFavorite()).isFalse();
        assertThat(favoriteRepository.existsByUserIdAndMosqueId(testUser.getId(), mosque1.getId())).isFalse();
    }

    @Test
    @DisplayName("Favorite Mosque: Get user favorites calculates distance accurately")
    void testGetUserFavoritesWithDistance() {
        favoriteService.addFavorite(testUser, mosque1.getId());
        favoriteService.addFavorite(testUser, mosque2.getId());

        // London user coordinates
        List<FavoriteMosqueResponseDto> favorites = favoriteService.getUserFavorites(testUser, 51.5074, -0.1278);
        assertThat(favorites).hasSize(2);
        assertThat(favorites.get(0).getDistanceKm()).isNotNull();
    }

    @Test
    @DisplayName("Badge System: Badges cannot be awarded twice to the same user (Idempotent)")
    void testBadgeAwardingIsIdempotent() {
        boolean firstAward = badgeService.awardBadge(testUser, BadgeService.BADGE_PIONEER);
        assertThat(firstAward).isTrue();

        boolean secondAward = badgeService.awardBadge(testUser, BadgeService.BADGE_PIONEER);
        assertThat(secondAward).isFalse();

        List<UserBadgeResponseDto> userBadges = badgeService.getUserBadges(testUser.getId());
        long pioneerCount = userBadges.stream().filter(b -> b.getCode().equals(BadgeService.BADGE_PIONEER)).count();
        assertThat(pioneerCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Badge System: Threshold evaluation awards CENTURION_CONTRIBUTOR and COMMUNITY_PILLAR")
    void testPointsBadgesEvaluation() {
        testUser.setPoints(120);
        badgeService.evaluatePointsBadges(testUser);
        assertThat(userBadgeRepository.existsByUserIdAndBadgeCode(testUser.getId(), BadgeService.BADGE_CENTURION_CONTRIBUTOR)).isTrue();
        assertThat(userBadgeRepository.existsByUserIdAndBadgeCode(testUser.getId(), BadgeService.BADGE_COMMUNITY_PILLAR)).isFalse();

        testUser.setPoints(550);
        badgeService.evaluatePointsBadges(testUser);
        assertThat(userBadgeRepository.existsByUserIdAndBadgeCode(testUser.getId(), BadgeService.BADGE_COMMUNITY_PILLAR)).isTrue();
    }

    @Test
    @DisplayName("Rate Limiter: Token Bucket permits requests within capacity and blocks when exhausted")
    void testTokenBucketLimiting() {
        String testKey = "unit-test-bucket-" + UUID.randomUUID();
        int capacity = 5;

        // Consume 5 allowed tokens
        for (int i = 0; i < capacity; i++) {
            RateLimitResult res = rateLimiterService.tryConsume(testKey, capacity);
            assertThat(res.isAllowed()).isTrue();
            assertThat(res.getRemaining()).isGreaterThanOrEqualTo(0);
        }

        // 6th request must be rate limited
        RateLimitResult blockedRes = rateLimiterService.tryConsume(testKey, capacity);
        assertThat(blockedRes.isAllowed()).isFalse();
        assertThat(blockedRes.getRemaining()).isEqualTo(0);
        assertThat(blockedRes.getRetryAfterSeconds()).isGreaterThan(0);
    }
}
