package com.openmosque.modules.mosque.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.mosque.dto.FacilityDto;
import com.openmosque.modules.mosque.service.FacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/facilities")
@RequiredArgsConstructor
@Tag(name = "Facilities", description = "Public list of standardized mosque facilities and amenities")
public class FacilityPublicController {

    private final FacilityService facilityService;

    @GetMapping
    @Operation(summary = "Get all active facilities", description = "Returns predefined Islamic amenities (Wudu area, Women section, Parking, Accessibility).")
    public ResponseEntity<ApiResponse<List<FacilityDto>>> getAllFacilities() {
        List<FacilityDto> facilities = facilityService.getAllActiveFacilities();
        return ResponseEntity.ok(ApiResponse.success(facilities, "Facilities retrieved successfully"));
    }
}
