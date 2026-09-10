package com.openmosque.modules.ingestion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h3>RadiusIngestRequestDto</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Transports administrative request parameters to trigger an automated ingestion of mosques
 * surrounding a specific GPS coordinate within a given radius in meters (using Overpass 'around' filter).
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Consumed by {@link com.openmosque.modules.ingestion.controller.OsmIngestionController#ingestByRadius}.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for ingesting mosques by GPS coordinate and radius")
public class RadiusIngestRequestDto {

    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    @Schema(description = "Center latitude", example = "51.5186")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    @Schema(description = "Center longitude", example = "-0.0654")
    private Double longitude;

    @NotNull(message = "Radius in meters is required")
    @Min(value = 100, message = "Radius must be at least 100 meters")
    @Max(value = 100000, message = "Radius cannot exceed 100,000 meters (100km)")
    @Schema(description = "Search radius in meters", example = "10000")
    @Builder.Default
    private Double radiusMeters = 10000.0;

    @Schema(description = "Fallback city name to assign if OSM tags lack addr:city", example = "London")
    private String defaultCity;

    @Schema(description = "Fallback country name to assign if OSM tags lack addr:country", example = "United Kingdom")
    private String defaultCountry;

    @Schema(description = "When true, parses data without persisting to the database", defaultValue = "false")
    @Builder.Default
    private boolean dryRun = false;
}
