package com.openmosque.modules.prayer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FridayJummahScheduleDto implements Serializable {
    private String firstJummahTime;      // "13:15"
    private String secondJummahTime;     // "14:00" (or null)
    private String khutbahLanguage;      // "English", "Arabic", "Urdu", etc.
}
