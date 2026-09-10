package com.openmosque.modules.claim.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Mosque Claim & Administrator Verification Request Entity.
 * 
 * WHY THIS IS PRESENT:
 * Fulfills Way B: Allows mosque committee members / imams to apply for official administrative control of their mosque.
 */
@Entity
@Table(name = "mosque_claim_requests")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueClaimRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mosque_id", nullable = false)
    private Mosque mosque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimant_id", nullable = false)
    private User claimant;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    @Column(name = "official_email", length = 255)
    private String officialEmail;

    /**
     * Role in mosque (e.g. 'Imam', 'Committee President', 'Trustee', 'General Secretary').
     */
    @Column(name = "position_in_mosque", nullable = false, length = 100)
    private String positionInMosque;

    /**
     * URL of verification proof (utility bill, charity registration, or committee letter).
     */
    @Column(name = "proof_document_url", columnDefinition = "TEXT")
    private String proofDocumentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ClaimStatus status = ClaimStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;
}
