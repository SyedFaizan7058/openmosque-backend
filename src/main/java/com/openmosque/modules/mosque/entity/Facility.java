package com.openmosque.modules.mosque.entity;

import com.openmosque.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Standard Islamic Mosque Amenity / Facility Entity (e.g. Wudu area, Parking, Women Section).
 * 
 * WHY THIS IS PRESENT:
 * Normalizes facilities to enable filterable searches (e.g. Find mosques with Women Section + Parking).
 */
@Entity
@Table(name = "facilities")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Facility extends BaseEntity {

    /**
     * Unique programmatic code identifier (e.g. 'WUDU_AREA', 'WOMENS_SECTION', 'PARKING').
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * User-facing readable name (e.g. "Women's Prayer Section").
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Detailed description of what the facility offers.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Icon identifier used by mobile & web UI (e.g. MaterialIcon / Lucide name).
     */
    @Column(name = "icon_name", length = 50)
    private String iconName;

    /**
     * Active state flag for facility selection.
     */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
