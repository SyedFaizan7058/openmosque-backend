package com.openmosque.modules.mosque.entity;

import com.openmosque.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Join entity connecting Mosques to Facilities with optional extra metadata (e.g. capacity, separate entrance details).
 */
@Entity
@Table(
        name = "mosque_facilities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"mosque_id", "facility_id"})
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueFacility extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mosque_id", nullable = false)
    private Mosque mosque;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    /**
     * Optional custom description (e.g. "Separate entrance on side street, 200 capacity").
     */
    @Column(name = "custom_details")
    private String customDetails;
}
