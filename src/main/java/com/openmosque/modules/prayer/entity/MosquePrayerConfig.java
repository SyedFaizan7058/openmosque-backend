package com.openmosque.modules.prayer.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.mosque.entity.Mosque;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Entity representing prayer times calculation configuration for a specific mosque.
 */
@Entity
@Table(name = "mosque_prayer_configs")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MosquePrayerConfig extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mosque_id", nullable = false, unique = true)
    private Mosque mosque;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false, length = 50)
    @Builder.Default
    private CalculationMethod calculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE;

    @Enumerated(EnumType.STRING)
    @Column(name = "juristic_school", nullable = false, length = 50)
    @Builder.Default
    private JuristicSchool juristicSchool = JuristicSchool.STANDARD;

    @Column(name = "time_zone", nullable = false, length = 100)
    @Builder.Default
    private String timeZone = "UTC";

    @Column(name = "fajr_angle")
    private Double fajrAngle;

    @Column(name = "isha_angle")
    private Double ishaAngle;

    @Column(name = "high_latitude_rule", length = 50)
    @Builder.Default
    private String highLatitudeRule = "MIDDLE_OF_THE_NIGHT";
}
