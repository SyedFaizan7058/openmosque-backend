package com.openmosque.modules.ingestion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * <h3>IngestionSummaryDto</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Returns a comprehensive audit and metrics report following an automated OpenStreetMap ingestion run.
 * Details total items discovered, how many new mosques were saved, how many duplicates were safely skipped
 * by the spatial deduplication engine, total facilities attached, and elapsed execution time.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Returned by {@link com.openmosque.modules.ingestion.service.OsmIngestionService} and
 * {@link com.openmosque.modules.ingestion.controller.OsmIngestionController}.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary report returned after an OSM batch ingestion run")
public class IngestionSummaryDto {

    @Schema(description = "Total raw OSM elements retrieved from Overpass API", example = "42")
    private int totalElementsFetched;

    @Schema(description = "Count of new mosques successfully inserted", example = "35")
    private int mosquesInserted;

    @Schema(description = "Count of existing mosques skipped due to proximity <= 50m or duplicate name", example = "7")
    private int duplicatesSkipped;

    @Schema(description = "Total Islamic amenities/facilities mapped and attached (e.g. Wudu, Women section)", example = "78")
    private int facilitiesAttached;

    @Schema(description = "Whether this run was executed in dry-run mode (no database writes)", example = "false")
    private boolean dryRun;

    @Schema(description = "List of new mosque names inserted or previewed")
    @Builder.Default
    private List<String> insertedMosqueNames = new ArrayList<>();

    @Schema(description = "Execution time in milliseconds", example = "1420")
    private long durationMs;
}
