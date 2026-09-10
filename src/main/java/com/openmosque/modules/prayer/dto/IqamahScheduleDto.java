package com.openmosque.modules.prayer.dto;

import com.openmosque.modules.prayer.entity.IqamahCalculationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IqamahScheduleDto implements Serializable {
    private UUID id;
    private UUID mosqueId;

    // Fajr
    private IqamahCalculationType fajrType;
    private Integer fajrOffsetMinutes;
    private LocalTime fajrFixedTime;

    // Dhuhr
    private IqamahCalculationType dhuhrType;
    private Integer dhuhrOffsetMinutes;
    private LocalTime dhuhrFixedTime;

    // Asr
    private IqamahCalculationType asrType;
    private Integer asrOffsetMinutes;
    private LocalTime asrFixedTime;

    // Maghrib
    private IqamahCalculationType maghribType;
    private Integer maghribOffsetMinutes;
    private LocalTime maghribFixedTime;

    // Isha
    private IqamahCalculationType ishaType;
    private Integer ishaOffsetMinutes;
    private LocalTime ishaFixedTime;

    // Custom Adhan Overrides (optional)
    private LocalTime fajrAdhanTime;
    private LocalTime dhuhrAdhanTime;
    private LocalTime asrAdhanTime;
    private LocalTime maghribAdhanTime;
    private LocalTime ishaAdhanTime;

    // Friday Jumu'ah
    private LocalTime jummah1Time;
    private LocalTime jummah2Time;
    private String jummahKhutbahLanguage;
}
