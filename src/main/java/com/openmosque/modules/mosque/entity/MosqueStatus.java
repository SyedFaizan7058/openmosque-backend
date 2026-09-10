package com.openmosque.modules.mosque.entity;

/**
 * Lifecycle status of a Mosque directory entry.
 */
public enum MosqueStatus {
    /**
     * Publicly visible and active in search and discovery.
     */
    ACTIVE,

    /**
     * Submitted by user and waiting for moderator approval.
     */
    PENDING_REVIEW,

    /**
     * Temporarily closed, demolished, or hidden by moderators.
     */
    INACTIVE
}
