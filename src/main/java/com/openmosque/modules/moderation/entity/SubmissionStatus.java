package com.openmosque.modules.moderation.entity;

public enum SubmissionStatus {
    /**
     * Waiting in queue for moderator review.
     */
    PENDING,

    /**
     * Approved by moderator and published to public directory.
     */
    APPROVED,

    /**
     * Rejected by moderator with feedback comments.
     */
    REJECTED
}
