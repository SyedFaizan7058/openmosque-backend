package com.openmosque.modules.mosque.entity;

import com.openmosque.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Mosque Gallery Image Entity.
 */
@Entity
@Table(name = "mosque_images")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mosque_id", nullable = false)
    private Mosque mosque;

    /**
     * URL of the image stored in GCP Cloud Storage.
     */
    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    /**
     * Descriptive caption (e.g. "Main Prayer Hall", "Exterior Front Entrance").
     */
    @Column(name = "caption")
    private String caption;

    /**
     * 'true' if this is the primary thumbnail / cover photo for mosque cards.
     */
    @Column(name = "is_cover", nullable = false)
    private boolean cover = false;

    /**
     * Display sort order in gallery carousel.
     */
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
}
