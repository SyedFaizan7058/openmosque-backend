package com.openmosque.modules.prayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Full daily prayer times and Iqamah response envelope.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrayerTimesDayResponseDto implements Serializable {
    private UUID mosqueId;
    private String mosqueName;
    private String mosqueSlug;
    private LocalDate date;
    private String hijriDate;             // e.g. "19 Safar 1448 AH"
    private String timeZone;
    private String calculationMethod;     // "MUSLIM_WORLD_LEAGUE"
    private String calculationMethodName; // "Muslim World League (MWL)"
    private String juristicSchool;        // "STANDARD" / "HANAFI"
    
    // Countdown state
    private String currentPrayer;         // "DHUHR"
    private String nextPrayer;            // "ASR"
    private String nextPrayerTime;        // "17:15"
    private Long timeRemainingMinutes;    // 45
    private String timeRemainingFormatted;// "in 45 mins"
    private String asrShafiTime;          // "15:41"
    private String asrHanafiTime;         // "16:45"
    private String ishraaqTime;           // "06:29" (Sunrise + 20m)
    private String chaashtTime;           // "09:12" (Midpoint Sunrise & Zawaal)
    private String zawaalTime;            // "12:12" (Dhuhr - 5m)
    private String sunsetTime;            // "18:24" (Maghrib - 3m)
    private String iftaarTime;            // "18:27" (Maghrib start)
    private String tahajjudTime;          // "03:11" (Fajr - 105m)
    private String sahoorEndTime;         // "04:46" (Fajr - 10m)

    // Prayer Slots (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha)
    private List<SinglePrayerTimeDto> timings;

    // Friday Jumu'ah Timetable
    private FridayJummahScheduleDto jummahSchedule;
}
