package com.openmosque.modules.prayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Representation of an individual prayer slot within a daily timetable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinglePrayerTimeDto implements Serializable {
    private String prayerName;        // FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA
    private String adhanTime;         // "05:15"
    private String iqamahTime;        // "05:35" (null for SUNRISE)
    private boolean isNext;           // true if this is the upcoming prayer
    private String timeRemainingFormatted; // "in 42 mins"
    private String asrShafiTime;      // "15:41" (Shafi'i / Standard Asr start time)
    private String asrHanafiTime;     // "16:45" (Hanafi Asr start time)
}
