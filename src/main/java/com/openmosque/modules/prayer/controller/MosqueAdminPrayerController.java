package com.openmosque.modules.prayer.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.prayer.dto.IqamahScheduleDto;
import com.openmosque.modules.prayer.dto.IqamahScheduleUpdateDto;
import com.openmosque.modules.prayer.dto.PrayerConfigDto;
import com.openmosque.modules.prayer.dto.PrayerConfigUpdateDto;
import com.openmosque.modules.prayer.service.PrayerTimesService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mosque-admin/mosques/{id}")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MOSQUE_ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Mosque Admin (Prayer Times Management)", description = "Endpoints for Imams and Mosque Admins to configure calculation methods, Iqamah offsets, and Friday Jumu'ah batches")
public class MosqueAdminPrayerController {

    private final PrayerTimesService prayerTimesService;

    @GetMapping("/prayer-config")
    @Operation(summary = "Get prayer calculation config", description = "Retrieves current calculation method, juristic school, and angles for a mosque.")
    public ResponseEntity<ApiResponse<PrayerConfigDto>> getPrayerConfig(
            @PathVariable("id") UUID mosqueId
    ) {
        PrayerConfigDto config = prayerTimesService.getPrayerConfig(mosqueId);
        return ResponseEntity.ok(ApiResponse.success(config, "Prayer configuration retrieved successfully"));
    }

    @PutMapping("/prayer-config")
    @Operation(summary = "Update prayer calculation config", description = "Updates calculation method (MWL, ISNA, Umm Al-Qura, Karachi) and juristic school (Hanafi/Standard).")
    public ResponseEntity<ApiResponse<PrayerConfigDto>> updatePrayerConfig(
            @PathVariable("id") UUID mosqueId,
            @Valid @RequestBody PrayerConfigUpdateDto dto,
            @CurrentUser User user
    ) {
        PrayerConfigDto updated = prayerTimesService.updatePrayerConfig(mosqueId, dto, user);
        return ResponseEntity.ok(ApiResponse.success(updated, "Prayer configuration updated successfully"));
    }

    @GetMapping("/iqamah-schedule")
    @Operation(summary = "Get Iqamah schedule", description = "Retrieves current Iqamah congregation rules, fixed times, offsets, and Jumu'ah batches.")
    public ResponseEntity<ApiResponse<IqamahScheduleDto>> getIqamahSchedule(
            @PathVariable("id") UUID mosqueId
    ) {
        IqamahScheduleDto schedule = prayerTimesService.getIqamahSchedule(mosqueId);
        return ResponseEntity.ok(ApiResponse.success(schedule, "Iqamah schedule retrieved successfully"));
    }

    @PutMapping("/iqamah-schedule")
    @Operation(summary = "Update Iqamah schedule", description = "Updates congregation offset minutes, fixed times, and Friday Jumu'ah batches.")
    public ResponseEntity<ApiResponse<IqamahScheduleDto>> updateIqamahSchedule(
            @PathVariable("id") UUID mosqueId,
            @Valid @RequestBody IqamahScheduleUpdateDto dto,
            @CurrentUser User user
    ) {
        IqamahScheduleDto updated = prayerTimesService.updateIqamahSchedule(mosqueId, dto, user);
        return ResponseEntity.ok(ApiResponse.success(updated, "Iqamah schedule updated successfully"));
    }
}
