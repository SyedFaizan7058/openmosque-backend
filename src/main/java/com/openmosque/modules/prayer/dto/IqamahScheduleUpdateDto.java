package com.openmosque.modules.prayer.dto;

import com.openmosque.modules.prayer.entity.IqamahCalculationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IqamahScheduleUpdateDto {

    // Fajr
    @NotNull(message = "Fajr calculation type is required")
    private IqamahCalculationType fajrType;
    private Integer fajrOffsetMinutes;
    @Schema(type = "string", example = "05:30:00")
    private LocalTime fajrFixedTime;

    // Dhuhr
    @NotNull(message = "Dhuhr calculation type is required")
    private IqamahCalculationType dhuhrType;
    private Integer dhuhrOffsetMinutes;
    @Schema(type = "string", example = "13:30:00")
    private LocalTime dhuhrFixedTime;

    // Asr
    @NotNull(message = "Asr calculation type is required")
    private IqamahCalculationType asrType;
    private Integer asrOffsetMinutes;
    @Schema(type = "string", example = "17:15:00")
    private LocalTime asrFixedTime;

    // Maghrib
    @NotNull(message = "Maghrib calculation type is required")
    private IqamahCalculationType maghribType;
    private Integer maghribOffsetMinutes;
    @Schema(type = "string", example = "19:45:00")
    private LocalTime maghribFixedTime;

    // Isha
    @NotNull(message = "Isha calculation type is required")
    private IqamahCalculationType ishaType;
    private Integer ishaOffsetMinutes;
    @Schema(type = "string", example = "21:15:00")
    private LocalTime ishaFixedTime;

    // Custom Adhan Overrides (optional, null to use astronomical calculated Adhan)
    @Schema(type = "string", example = "05:00:00")
    private LocalTime fajrAdhanTime;
    @Schema(type = "string", example = "12:30:00")
    private LocalTime dhuhrAdhanTime;
    @Schema(type = "string", example = "16:45:00")
    private LocalTime asrAdhanTime;
    @Schema(type = "string", example = "19:15:00")
    private LocalTime maghribAdhanTime;
    @Schema(type = "string", example = "20:45:00")
    private LocalTime ishaAdhanTime;

    // Friday Jumu'ah Batches
    @Schema(type = "string", example = "13:15:00")
    private LocalTime jummah1Time;
    @Schema(type = "string", example = "14:00:00")
    private LocalTime jummah2Time;
    private String jummahKhutbahLanguage;
}
