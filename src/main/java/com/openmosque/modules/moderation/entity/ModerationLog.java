package com.openmosque.modules.moderation.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Moderation Audit Log Entity.
 * 
 * WHY THIS IS PRESENT:
 * Keeps an immutable audit trail of who approved/rejected submissions and when.
 */
@Entity
@Table(name = "moderation_logs")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private MosqueSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id", nullable = false)
    private User moderator;

    /**
     * Action taken: 'APPROVED', 'REJECTED', 'COMMENTED'.
     */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    /**
     * Internal notes or justification.
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
