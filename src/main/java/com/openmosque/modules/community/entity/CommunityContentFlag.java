package com.openmosque.modules.community.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * <h3>CommunityContentFlag</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Powers the crowd-sourced moderation pipeline. Allows any registered user to flag reviews, questions,
 * or answers for inappropriate content, profanity, harassment, or spam. Flags enter a moderation queue
 * where community moderators or super administrators review the report and take action (HIDE, RESOLVE, DISMISS).
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * <ul>
 *   <li>Database table: {@code community_content_flags} (created via Flyway V7 migration).</li>
 *   <li>Service layer: {@link com.openmosque.modules.community.service.CommunityModerationService} for queue management and resolution.</li>
 *   <li>REST APIs: {@link com.openmosque.modules.community.controller.CommunityUserController} (reporting content)
 *       and {@link com.openmosque.modules.community.controller.CommunityAdminModerationController} (moderator dashboard).</li>
 *   <li>Mapper: {@link com.openmosque.modules.community.mapper.CommunityMapper}.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "community_content_flags")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityContentFlag extends BaseEntity {

    /** The type of entity flagged (REVIEW, QUESTION, ANSWER). */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private FlagStatus status = FlagStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "reviewer_notes", columnDefinition = "TEXT")
    private String reviewerNotes;
}
