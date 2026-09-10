package com.openmosque.modules.community.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <h3>MosqueReview</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Allows registered worshippers to leave reviews and category-specific ratings (overall, cleanliness,
 * facilities, sisters' area, and parking) for a mosque. Enforces a strict one-review-per-user-per-mosque
 * database constraint (with upsert/update semantics in the service layer).
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * <ul>
 *   <li>Database table: {@code mosque_reviews} (with unique index {@code uq_mosque_reviews_user}).</li>
 *   <li>Service layer: {@link com.openmosque.modules.community.service.CommunityReviewService} for submission, calculations, and deletion.</li>
 *   <li>REST APIs: {@link com.openmosque.modules.community.controller.CommunityPublicController} (public review feed & aggregates)
 *       and {@link com.openmosque.modules.community.controller.CommunityUserController} (authenticated user submissions).</li>
 *   <li>Mapper: {@link com.openmosque.modules.community.mapper.CommunityMapper}.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "mosque_reviews", uniqueConstraints = {
        @UniqueConstraint(name = "uq_mosque_reviews_user", columnNames = {"mosque_id", "user_id"})
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueReview extends BaseEntity {

    /** The mosque being reviewed. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mosque_id", nullable = false)
    private Mosque mosque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rating_overall", nullable = false)
    private int ratingOverall;

    @Column(name = "rating_cleanliness")
    private Integer ratingCleanliness;

    @Column(name = "rating_facilities")
    private Integer ratingFacilities;

    @Column(name = "rating_womens_area")
    private Integer ratingWomensArea;

    @Column(name = "rating_parking")
    private Integer ratingParking;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ContentStatus status = ContentStatus.PUBLISHED;
}
