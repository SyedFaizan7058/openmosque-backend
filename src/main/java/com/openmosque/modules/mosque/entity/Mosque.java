package com.openmosque.modules.mosque.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Core Mosque Domain Entity.
 * 
 * WHY THIS IS PRESENT:
 * Represents a registered mosque profile containing geographic coordinates,
 * facilities/amenities, gallery photos, and live broadcast information.
 */
@Entity
@Table(name = "mosques")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Mosque extends BaseEntity {

    /**
     * Official name of the mosque (e.g. "East London Mosque", "Islamic Center of Washington").
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * URL-friendly unique slug (e.g. "east-london-mosque-whitechapel").
     */
    @Column(name = "slug", nullable = false, unique = true, length = 250)
    private String slug;

    /**
     * Full description, history, and community details.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Street address.
     */
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

    /**
     * PostGIS 2D Point Geometry in WGS84 (SRID 4326).
     * Enables ultra-fast GIST spatial indexing and ST_DWithin radius searches.
     */
    @Column(name = "location", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    /**
     * Live stream broadcast link (e.g. YouTube Live, Mixlr, Facebook Live for Jummah Khutbah).
     */
    @Column(name = "live_stream_url", length = 500)
    private String liveStreamUrl;

    /**
     * Mosque Administrator Verification badge flag.
     */
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    /**
     * Directory status: ACTIVE, PENDING_REVIEW, INACTIVE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private MosqueStatus status = MosqueStatus.ACTIVE;

    /**
     * Submitter / Contributor who added this mosque.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @OneToMany(mappedBy = "mosque", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MosqueFacility> facilities = new ArrayList<>();

    @OneToMany(mappedBy = "mosque", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MosqueImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "mosque", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserFavoriteMosque> favorites = new ArrayList<>();

    /**
     * Helper to add a facility association.
     */
    public void addFacility(Facility facility, String customDetails) {
        MosqueFacility mf = MosqueFacility.builder()
                .mosque(this)
                .facility(facility)
                .customDetails(customDetails)
                .build();
        this.facilities.add(mf);
    }

    /**
     * Helper to add an image.
     */
    public void addImage(String imageUrl, String caption, boolean isCover, int order) {
        MosqueImage image = MosqueImage.builder()
                .mosque(this)
                .imageUrl(imageUrl)
                .caption(caption)
                .cover(isCover)
                .displayOrder(order)
                .build();
        this.images.add(image);
    }
}
