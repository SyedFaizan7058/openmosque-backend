package com.openmosque.modules.ingestion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h3>CityIngestRequestDto</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Transports administrative request parameters to trigger an automated batch ingestion of all mosques
 * within a specified city and country from OpenStreetMap. Supports a dry-run preview mode.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Consumed by {@link com.openmosque.modules.ingestion.controller.OsmIngestionController#ingestByCity}.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for ingesting mosques by city and country")
public class CityIngestRequestDto {

    @NotBlank(message = "City name is required")
    @Schema(description = "City name to query", example = "London")
    private String city;

    @Schema(description = "Optional country name or ISO country code to narrow the search area", example = "United Kingdom")
    private String country;

    @Schema(description = "When true, scans and parses OSM data without persisting any changes to the database", defaultValue = "false")
    @Builder.Default
    private boolean dryRun = false;
}
