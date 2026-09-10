package com.openmosque.modules.prayer.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.mosque.entity.Mosque;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;

/**
 * Entity representing Iqamah congregation schedule and overrides for a mosque.
 */
@Entity
@Table(name = "mosque_iqamah_schedules")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueIqamahSchedule extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mosque_id", nullable = false, unique = true)
    private Mosque mosque;

    // Fajr
    @Enumerated(EnumType.STRING)
    @Column(name = "fajr_type", nullable = false, length = 50)
    @Builder.Default
    private IqamahCalculationType fajrType = IqamahCalculationType.OFFSET_AFTER_ADHAN;

    @Column(name = "fajr_offset_minutes")
    @Builder.Default
    private Integer fajrOffsetMinutes = 20;

    @Column(name = "fajr_fixed_time")
    private LocalTime fajrFixedTime;

    // Dhuhr
    @Enumerated(EnumType.STRING)
    @Column(name = "dhuhr_type", nullable = false, length = 50)
    @Builder.Default
    private IqamahCalculationType dhuhrType = IqamahCalculationType.OFFSET_AFTER_ADHAN;

    @Column(name = "dhuhr_offset_minutes")
    @Builder.Default
    private Integer dhuhrOffsetMinutes = 15;

    @Column(name = "dhuhr_fixed_time")
    @Builder.Default
    private LocalTime dhuhrFixedTime = LocalTime.of(13, 30);

    // Asr
    @Enumerated(EnumType.STRING)
    @Column(name = "asr_type", nullable = false, length = 50)
    @Builder.Default
    private IqamahCalculationType asrType = IqamahCalculationType.OFFSET_AFTER_ADHAN;

    @Column(name = "asr_offset_minutes")
    @Builder.Default
    private Integer asrOffsetMinutes = 15;

    @Column(name = "asr_fixed_time")
    private LocalTime asrFixedTime;

    // Maghrib
    @Enumerated(EnumType.STRING)
    @Column(name = "maghrib_type", nullable = false, length = 50)
    @Builder.Default
    private IqamahCalculationType maghribType = IqamahCalculationType.OFFSET_AFTER_ADHAN;

    @Column(name = "maghrib_offset_minutes")
    @Builder.Default
    private Integer maghribOffsetMinutes = 10;

    @Column(name = "maghrib_fixed_time")
    private LocalTime maghribFixedTime;

    // Isha
    @Enumerated(EnumType.STRING)
    @Column(name = "isha_type", nullable = false, length = 50)
    @Builder.Default
    private IqamahCalculationType ishaType = IqamahCalculationType.OFFSET_AFTER_ADHAN;

    @Column(name = "isha_offset_minutes")
    @Builder.Default
    private Integer ishaOffsetMinutes = 15;

    @Column(name = "isha_fixed_time")
    private LocalTime ishaFixedTime;

    // Optional Custom Adhan Times (null indicates automatic astronomical calculation)
    @Column(name = "fajr_adhan_time")
    private LocalTime fajrAdhanTime;

    @Column(name = "dhuhr_adhan_time")
    private LocalTime dhuhrAdhanTime;

    @Column(name = "asr_adhan_time")
    private LocalTime asrAdhanTime;

    @Column(name = "maghrib_adhan_time")
    private LocalTime maghribAdhanTime;

    @Column(name = "isha_adhan_time")
    private LocalTime ishaAdhanTime;

    // Friday Jumu'ah Batches
    @Column(name = "jummah_1_time")
    @Builder.Default
    private LocalTime jummah1Time = LocalTime.of(13, 15);

    @Column(name = "jummah_2_time")
    private LocalTime jummah2Time;

    @Column(name = "jummah_khutbah_language", length = 50)
    @Builder.Default
    private String jummahKhutbahLanguage = "Arabic";
}
