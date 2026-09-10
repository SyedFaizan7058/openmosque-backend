package com.openmosque.modules.prayer.dto;

import com.openmosque.modules.prayer.entity.CalculationMethod;
import com.openmosque.modules.prayer.entity.JuristicSchool;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrayerConfigUpdateDto {

    @NotNull(message = "Calculation method is required")
    private CalculationMethod calculationMethod;

    @NotNull(message = "Juristic school is required")
    private JuristicSchool juristicSchool;

    private String timeZone;
    private Double fajrAngle;
    private Double ishaAngle;
    private String highLatitudeRule;
}
