package com.openmosque.modules.moderation.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Crowdsourced Mosque Submission Entity.
 * 
 * WHY THIS IS PRESENT:
 * Implements the crowdsourcing and verification workflow from the business plan.
 * Users submit new mosques or corrections, which sit in a pending moderation queue
 * before being approved and converted into verified directory entries.
 */
@Entity
@Table(name = "mosque_submissions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueSubmission extends BaseEntity {

    /**
     * User who contributed/submitted the mosque details.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter_id", nullable = false)
    private User submitter;

    /**
     * If this is an EDIT_SUGGESTION, points to the existing mosque being edited. Null for NEW_MOSQUE.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_mosque_id")
    private Mosque targetMosque;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_type", nullable = false, length = 50)
    @Builder.Default
    private SubmissionType submissionType = SubmissionType.NEW_MOSQUE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address", nullable = false, length = 300)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "live_stream_url", length = 500)
    private String liveStreamUrl;

    /**
     * Array of facility codes selected by user (e.g. ['WUDU_AREA', 'WOMENS_SECTION', 'PARKING']).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "submission_facility_codes", joinColumns = @JoinColumn(name = "submission_id"))
    @Column(name = "facility_code")
    @Builder.Default
    private List<String> facilityCodes = new ArrayList<>();

    /**
     * Uploaded image URLs for mosque verification.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "submission_image_urls", joinColumns = @JoinColumn(name = "submission_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    /**
     * Moderator who reviewed the submission.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    /**
     * Feedback or rejection reason provided by the moderator.
     */
    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;
}
