package com.openmosque.modules.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * <h3>OsmResponseDto</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Represents the root JSON payload returned by the OpenStreetMap Overpass API interpreter.
 * Encapsulates the version, generator metadata, and array of geographic {@link OsmElementDto} items.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Deserialized by {@link com.openmosque.modules.ingestion.client.OverpassApiClient} and consumed by
 * {@link com.openmosque.modules.ingestion.service.OsmIngestionService}.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsmResponseDto {

    @JsonProperty("version")
    private Double version;

    @JsonProperty("generator")
    private String generator;

    @JsonProperty("elements")
    @Builder.Default
    private List<OsmElementDto> elements = new ArrayList<>();
}
