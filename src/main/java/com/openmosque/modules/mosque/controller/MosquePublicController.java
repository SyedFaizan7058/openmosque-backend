package com.openmosque.modules.mosque.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.mosque.dto.MosqueResponseDto;
import com.openmosque.modules.mosque.dto.MosqueSummaryDto;
import com.openmosque.modules.mosque.dto.MosqueUpdateRequestDto;
import com.openmosque.modules.mosque.service.MosqueService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Public REST Controller for Mosque Directory Discovery, PostGIS Radius Searches, and Profiles.
 */
@RestController
@RequestMapping("/api/v1/mosques")
@RequiredArgsConstructor
@Tag(name = "Mosque Discovery", description = "Public endpoints for nearby mosque search, keyword filtering, and profile retrieval")
public class MosquePublicController {

    private final MosqueService mosqueService;

    /**
     * PostGIS Spatial Radius Query: Finds mosques nearby GPS coordinates.
     */
    @GetMapping("/nearby")
    @Operation(
            summary = "Find nearby mosques using PostGIS spatial radius",
            description = "Calculates geodesic distances from user GPS coordinates and filters by facilities."
    )
    public ResponseEntity<ApiResponse<List<MosqueSummaryDto>>> getNearbyMosques(
            @Parameter(description = "User GPS Latitude [-90.0, 90.0]", required = true, example = "51.5173")
            @RequestParam("latitude") double latitude,

            @Parameter(description = "User GPS Longitude [-180.0, 180.0]", required = true, example = "-0.0658")
            @RequestParam("longitude") double longitude,

            @Parameter(description = "Search radius in kilometers (default 10km)", example = "10.0")
            @RequestParam(value = "radiusKm", defaultValue = "10.0") double radiusKm,

            @Parameter(description = "Optional facility codes filter (e.g. ['WOMENS_SECTION', 'PARKING'])")
            @RequestParam(value = "facilities", required = false) List<String> facilities
    ) {
        List<MosqueSummaryDto> mosques = mosqueService.getNearbyMosques(latitude, longitude, radiusKm, facilities);
        return ResponseEntity.ok(ApiResponse.success(mosques, "Nearby mosques retrieved successfully"));
    }

    /**
     * Search and Filter Mosques with Pagination.
     */
    @GetMapping({"", "/", "/search"})
    @Operation(
            summary = "Search mosques by text, city, or country",
            description = "Paginated keyword and location filtering."
    )
    public ResponseEntity<ApiResponse<PageResponse<MosqueSummaryDto>>> searchMosques(
            @Parameter(description = "Search query keyword matching name or address")
            @RequestParam(value = "q", required = false) String query,

            @Parameter(description = "Filter by City")
            @RequestParam(value = "city", required = false) String city,

            @Parameter(description = "Filter by Country")
            @RequestParam(value = "country", required = false) String country,

            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(description = "Page size (default 20)", example = "20")
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        PageResponse<MosqueSummaryDto> results = mosqueService.searchMosques(query, city, country, pageable);
        return ResponseEntity.ok(ApiResponse.success(results, "Search completed successfully"));
    }

    /**
     * Get Full Mosque Profile by UUID or Slug.
     */
    @GetMapping("/{idOrSlug}")
    @Operation(
            summary = "Get full mosque profile",
            description = "Fetches mosque details, prayer broadcast links, amenities, and photo gallery."
    )
    public ResponseEntity<ApiResponse<MosqueResponseDto>> getMosqueDetails(
            @Parameter(description = "Mosque UUID or unique URL slug", required = true, example = "east-london-mosque")
            @PathVariable("idOrSlug") String idOrSlug
    ) {
        MosqueResponseDto mosque = mosqueService.getMosqueByIdOrSlug(idOrSlug);
        return ResponseEntity.ok(ApiResponse.success(mosque, "Mosque profile retrieved successfully"));
    }

    /**
     * Updates an existing Mosque.
     * Permitted for approved Mosque Administrators or Super Admins.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MOSQUE_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update mosque details", description = "Updates mosque profile information. Permitted for approved Mosque Administrators or Super Admins.")
    public ResponseEntity<ApiResponse<MosqueResponseDto>> updateMosque(
            @PathVariable("id") UUID mosqueId,
            @Valid @RequestBody MosqueUpdateRequestDto request,
            @CurrentUser User user
    ) {
        MosqueResponseDto response = mosqueService.updateMosque(mosqueId, request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Mosque updated successfully"));
    }
}
