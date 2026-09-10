package com.openmosque.modules.prayer.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.prayer.dto.CalculationMethodDto;
import com.openmosque.modules.prayer.dto.PrayerTimesDayResponseDto;
import com.openmosque.modules.prayer.service.PrayerTimesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Prayer Times Engine (Public)", description = "Endpoints for calculating astronomical prayer times, Iqamah congregation schedules, and next prayer countdowns")
public class PrayerTimesPublicController {

    private final PrayerTimesService prayerTimesService;

    @GetMapping("/mosques/{idOrSlug}/prayer-times")
    @Operation(summary = "Get daily prayer & Iqamah schedule", description = "Calculates astronomical Adhan times, applies mosque-specific Iqamah overrides, and computes next prayer countdown.")
    public ResponseEntity<ApiResponse<PrayerTimesDayResponseDto>> getPrayerTimes(
            @PathVariable("idOrSlug") String idOrSlug,
            @Parameter(description = "Target Date (YYYY-MM-DD), defaults to today")
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        PrayerTimesDayResponseDto response = prayerTimesService.getPrayerTimesForMosque(idOrSlug, date);
        return ResponseEntity.ok(ApiResponse.success(response, "Prayer times retrieved successfully"));
    }

    @GetMapping("/prayer-times/methods")
    @Operation(summary = "List supported calculation methods", description = "Retrieves all standard Islamic calculation methods (ISNA, MWL, Umm Al-Qura, Karachi, Egypt, etc.).")
    public ResponseEntity<ApiResponse<List<CalculationMethodDto>>> getCalculationMethods() {
        List<CalculationMethodDto> methods = prayerTimesService.getSupportedCalculationMethods();
        return ResponseEntity.ok(ApiResponse.success(methods, "Calculation methods retrieved successfully"));
    }
}
