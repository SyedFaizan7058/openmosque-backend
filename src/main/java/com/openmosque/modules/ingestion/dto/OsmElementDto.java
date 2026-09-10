package com.openmosque.modules.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * <h3>OsmElementDto</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Represents an individual OpenStreetMap geographic element returned by the Overpass API.
 * An element can be either a 'node' (single GPS point) or a 'way' (building polygon with a computed center).
 * Contains tags with amenity metadata, addresses, contacts, and wheelchair/gender access keys.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Parsed by {@link com.openmosque.modules.ingestion.service.OsmIngestionService} from {@link OsmResponseDto}.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsmElementDto {

    @JsonProperty("type")
    private String type; // "node", "way", "relation"

    @JsonProperty("id")
    private Long id;

    @JsonProperty("lat")
    private Double lat;

    @JsonProperty("lon")
    private Double lon;

    /**
     * For OSM 'way' polygons, Overpass 'out center' returns the computed center coordinates.
     */
    @JsonProperty("center")
    private OsmCenterDto center;

    @JsonProperty("tags")
    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    /**
     * Resolves the latitude whether the element is a node (direct lat) or a way (center.lat).
     */
    public Double getEffectiveLatitude() {
        if (lat != null) return lat;
        if (center != null) return center.getLat();
        return null;
    }

    /**
     * Resolves the longitude whether the element is a node (direct lon) or a way (center.lon).
     */
    public Double getEffectiveLongitude() {
        if (lon != null) return lon;
        if (center != null) return center.getLon();
        return null;
    }

    /**
     * Represents the center coordinate of an OSM way building polygon.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OsmCenterDto {
        @JsonProperty("lat")
        private Double lat;

        @JsonProperty("lon")
        private Double lon;
    }
}
