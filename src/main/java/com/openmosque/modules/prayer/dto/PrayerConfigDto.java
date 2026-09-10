package com.openmosque.modules.prayer.dto;

import com.openmosque.modules.prayer.entity.CalculationMethod;
import com.openmosque.modules.prayer.entity.JuristicSchool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrayerConfigDto implements Serializable {
    private UUID id;
    private UUID mosqueId;
    private CalculationMethod calculationMethod;
    private String calculationMethodDisplayName;
    private JuristicSchool juristicSchool;
    private String timeZone;
    private Double fajrAngle;
    private Double ishaAngle;
    private String highLatitudeRule;
}
