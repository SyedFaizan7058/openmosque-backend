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
 * <h3>BboxIngestRequestDto</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Transports administrative request parameters to trigger an automated batch ingestion of mosques
 * within a rectangular bounding box (South, West, North, East).
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Consumed by {@link com.openmosque.modules.ingestion.controller.OsmIngestionController#ingestByBoundingBox}.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for ingesting mosques within a geographic bounding box")
public class BboxIngestRequestDto {

    @NotNull(message = "South latitude is required")
    @Min(-90) @Max(90)
    @Schema(description = "South latitude (min lat)", example = "51.45")
    private Double south;

    @NotNull(message = "West longitude is required")
    @Min(-180) @Max(180)
    @Schema(description = "West longitude (min lon)", example = "-0.20")
    private Double west;

    @NotNull(message = "North latitude is required")
    @Min(-90) @Max(90)
    @Schema(description = "North latitude (max lat)", example = "51.55")
    private Double north;

    @NotNull(message = "East longitude is required")
    @Min(-180) @Max(180)
    @Schema(description = "East longitude (max lon)", example = "0.05")
    private Double east;

    @Schema(description = "Fallback city name if OSM tags lack addr:city", example = "London")
    private String defaultCity;

    @Schema(description = "Fallback country name if OSM tags lack addr:country", example = "United Kingdom")
    private String defaultCountry;

    @Schema(description = "When true, parses data without persisting changes to the database", defaultValue = "false")
    @Builder.Default
    private boolean dryRun = false;
}
