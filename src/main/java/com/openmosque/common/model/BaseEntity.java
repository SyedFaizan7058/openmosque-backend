package com.openmosque.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Abstract Base MappedSuperclass for all JPA persistent entities in the OpenMosque project.
 * 
 * WHY THIS IS PRESENT:
 * 1. Code Reuse: Prevents repeating UUID primary keys, audit timestamps, and soft-delete fields across entities.
 * 2. Audit Trail: Automatically tracks 'createdAt' and 'updatedAt' upon insertion and modification.
 * 3. Soft Deletion: Supports 'is_deleted' flag for safe data retention and moderation audits.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    /**
     * Globally unique UUID primary key generated at application level.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * UTC Timestamp of when this record was originally created. Managed automatically by Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * UTC Timestamp of the last update to this record. Managed automatically by Hibernate.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Soft-delete flag: 'true' if the record has been archived/deleted, 'false' if active.
     */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    /**
     * Pre-persist lifecycle callback guaranteeing timestamps are populated before SQL INSERT.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    /**
     * Pre-update lifecycle callback updating the timestamp before SQL UPDATE.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
