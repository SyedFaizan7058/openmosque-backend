package com.openmosque.modules.event.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * <h3>MosqueEvent</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Represents community programs, lectures, youth circles, charity fundraisers, and religious gatherings
 * hosted by or affiliated with a specific mosque. Enables worshippers to discover upcoming events,
 * view speakers, check target audience (Sisters only, Brothers only, Youth, All), and obtain registration links.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * <ul>
 *   <li>Database table: {@code mosque_events} (created via Flyway V7 migration).</li>
 *   <li>Service layer: {@link com.openmosque.modules.event.service.MosqueEventService} for CRUD, date-filtering, and cancellation.</li>
 *   <li>REST APIs: {@link com.openmosque.modules.event.controller.EventPublicController} (public discovery)
 *       and {@link com.openmosque.modules.event.controller.MosqueAdminEventController} (mosque administration).</li>
 *   <li>Mapper: {@link com.openmosque.modules.event.mapper.EventMapper} for DTO conversions.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "mosque_events")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueEvent extends BaseEntity {

    /** The mosque hosting this event. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mosque_id", nullable = false)
    private Mosque mosque;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience", nullable = false, length = 50)
    @Builder.Default
    private EventAudience audience = EventAudience.ALL;

    @Column(name = "start_date_time", nullable = false)
    private Instant startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private Instant endDateTime;

    @Column(name = "location_details", length = 255)
    private String locationDetails;

    @Column(name = "speaker_name", length = 150)
    private String speakerName;

    @Column(name = "banner_image_url", length = 500)
    private String bannerImageUrl;

    @Column(name = "registration_url", length = 500)
    private String registrationUrl;

    @Column(name = "is_cancelled", nullable = false)
    @Builder.Default
    private boolean cancelled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
}
