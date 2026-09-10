package com.openmosque.modules.ingestion.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.common.util.SecurityUtils;
import com.openmosque.security.annotation.CurrentUser;
import com.openmosque.modules.ingestion.dto.*;
import com.openmosque.modules.ingestion.service.OsmIngestionService;
import com.openmosque.modules.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h3>OsmIngestionController</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Provides administrative REST endpoints to trigger automated batch ingestion of mosques from the
 * OpenStreetMap (OSM) Overpass API. Allows community moderators and super administrators to populate
 * entire cities, surrounding radii, or bounding boxes with verified spatial coordinates, addresses,
 * and amenities without manual data entry.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Consumed by administrator dashboard and automated backend maintenance pipelines.
 * Endpoints:
 * <ul>
 *   <li>{@code POST /api/v1/admin/ingest/osm/city}</li>
 *   <li>{@code POST /api/v1/admin/ingest/osm/radius}</li>
 *   <li>{@code POST /api/v1/admin/ingest/osm/bbox}</li>
 * </ul>
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ingest/osm")
@RequiredArgsConstructor
@Tag(name = "OpenStreetMap Ingestion Admin API", description = "Automated Overpass batch ingestion and data synchronization")
public class OsmIngestionController {

    private final OsmIngestionService osmIngestionService;

    /**
     * Triggers batch ingestion of all mosques located within an administrative city boundary.
     * Restricted to ROLE_MODERATOR or ROLE_SUPER_ADMIN.
     */
    @PostMapping("/city")
    @Operation(summary = "Ingest mosques by city name", description = "Queries OpenStreetMap Overpass API for all Muslim places of worship within the given city and country.")
    public ResponseEntity<ApiResponse<IngestionSummaryDto>> ingestByCity(
            @Valid @RequestBody CityIngestRequestDto request,
            @CurrentUser User currentUser
    ) {
        SecurityUtils.requireModeratorOrSuperAdmin(currentUser, "trigger OSM city ingestion");
        log.info("Admin user '{}' triggered OSM ingestion for city: '{}', country: '{}' (dryRun: {})",
                currentUser.getEmail(), request.getCity(), request.getCountry(), request.isDryRun());

        IngestionSummaryDto result = osmIngestionService.ingestByCity(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Triggers batch ingestion of all mosques surrounding a GPS coordinate within a radius in meters.
     * Restricted to ROLE_MODERATOR or ROLE_SUPER_ADMIN.
     */
    @PostMapping("/radius")
    @Operation(summary = "Ingest mosques by center coordinate and radius", description = "Queries OpenStreetMap Overpass API within a specified radius around a GPS point.")
    public ResponseEntity<ApiResponse<IngestionSummaryDto>> ingestByRadius(
            @Valid @RequestBody RadiusIngestRequestDto request,
            @CurrentUser User currentUser
    ) {
        SecurityUtils.requireModeratorOrSuperAdmin(currentUser, "trigger OSM radius ingestion");
        log.info("Admin user '{}' triggered OSM ingestion for lat: {}, lon: {}, radius: {}m (dryRun: {})",
                currentUser.getEmail(), request.getLatitude(), request.getLongitude(), request.getRadiusMeters(), request.isDryRun());

        IngestionSummaryDto result = osmIngestionService.ingestByRadius(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Triggers batch ingestion of all mosques within a geographic bounding box.
     * Restricted to ROLE_MODERATOR or ROLE_SUPER_ADMIN.
     */
    @PostMapping("/bbox")
    @Operation(summary = "Ingest mosques by geographic bounding box", description = "Queries OpenStreetMap Overpass API within a bounding box [south, west, north, east].")
    public ResponseEntity<ApiResponse<IngestionSummaryDto>> ingestByBoundingBox(
            @Valid @RequestBody BboxIngestRequestDto request,
            @CurrentUser User currentUser
    ) {
        SecurityUtils.requireModeratorOrSuperAdmin(currentUser, "trigger OSM bounding box ingestion");
        log.info("Admin user '{}' triggered OSM ingestion for bbox: [{}, {}, {}, {}] (dryRun: {})",
                currentUser.getEmail(), request.getSouth(), request.getWest(), request.getNorth(), request.getEast(), request.isDryRun());

        IngestionSummaryDto result = osmIngestionService.ingestByBoundingBox(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
