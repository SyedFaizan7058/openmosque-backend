package com.openmosque.modules.event.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.mosque.entity.Mosque;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * <h3>MosqueKhutbah</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Represents Friday (Jumu'ah) sermons for a mosque. Large urban mosques routinely conduct
 * multiple shifts/batches of Friday prayers (e.g. 1st batch at 12:30 PM in Arabic/English, 2nd batch at 1:30 PM).
 * This entity captures the topic, Khatib (speaker), shift number, prayer timings, language, and live stream/recording links.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * <ul>
 *   <li>Database table: {@code mosque_khutbahs} (created via Flyway V7 migration).</li>
 *   <li>Service layer: {@link com.openmosque.modules.event.service.MosqueKhutbahService} for publishing and scheduling.</li>
 *   <li>REST APIs: {@link com.openmosque.modules.event.controller.EventPublicController} (upcoming Friday khutbahs)
 *       and {@link com.openmosque.modules.event.controller.MosqueAdminEventController} (admin schedule management).</li>
 *   <li>Mapper: {@link com.openmosque.modules.event.mapper.EventMapper}.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "mosque_khutbahs")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueKhutbah extends BaseEntity {

    /** The mosque delivering this Friday sermon. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mosque_id", nullable = false)
    private Mosque mosque;

    @Column(name = "khutbah_date", nullable = false)
    private LocalDate khutbahDate;

    @Column(name = "topic", nullable = false, length = 255)
    private String topic;

    @Column(name = "khatib_name", nullable = false, length = 150)
    private String khatibName;

    @Column(name = "batch_number", nullable = false)
    @Builder.Default
    private int batchNumber = 1;

    @Column(name = "khutbah_time", nullable = false)
    private LocalTime khutbahTime;

    @Column(name = "adhaan_time")
    private LocalTime adhaanTime;

    @Column(name = "iqamah_time")
    private LocalTime iqamahTime;

    @Column(name = "language", length = 100)
    @Builder.Default
    private String language = "English";

    @Column(name = "stream_url", length = 500)
    private String streamUrl;

    @Column(name = "recording_url", length = 500)
    private String recordingUrl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
