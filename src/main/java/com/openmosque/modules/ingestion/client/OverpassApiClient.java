package com.openmosque.modules.ingestion.client;

import com.openmosque.common.exception.BadRequestException;
import com.openmosque.modules.ingestion.dto.OsmResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * <h3>OverpassApiClient</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * HTTP Client communicating with the OpenStreetMap Overpass API interpreter.
 * Executes Overpass QL queries to extract Islamic places of worship (nodes and ways with tags)
 * for specified geographic areas, cities, radii, or bounding boxes. Configured with a compliant
 * User-Agent header and defensive timeout policies as mandated by the OSM Fair Use Policy.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Injected into {@link com.openmosque.modules.ingestion.service.OsmIngestionService} to retrieve raw OSM geometries.
 * </p>
 */
@Slf4j
@Component
public class OverpassApiClient {

    private final RestClient restClient;
    private final String userAgent;

    public OverpassApiClient(
            @Value("${app.osm.overpass.base-url:https://overpass-api.de/api/interpreter}") String baseUrl,
            @Value("${app.osm.overpass.connect-timeout-seconds:10}") int connectTimeout,
            @Value("${app.osm.overpass.read-timeout-seconds:45}") int readTimeout,
            @Value("${app.osm.overpass.user-agent:OpenMosque-Ingestion-Bot/1.0 (https://openmosque.org; contact@openmosque.org)}") String userAgent
    ) {
        this.userAgent = userAgent;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(connectTimeout));
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeout));

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .build();
    }

    /**
     * Queries OpenStreetMap for all mosques located within an administrative city boundary.
     *
     * @param city City name (e.g. "London", "Toronto", "Istanbul")
     * @param country Optional country filter
     * @return Deserialized OSM response containing matching nodes and ways
     */
    public OsmResponseDto fetchByCity(String city, String country) {
        String sanitizedCity = escapeOverpassString(city);
        StringBuilder query = new StringBuilder();
        query.append("[out:json][timeout:45];\n");

        if (country != null && !country.isBlank()) {
            String sanitizedCountry = escapeOverpassString(country);
            query.append("area[\"name\"=\"").append(sanitizedCountry).append("\"]->.countryArea;\n");
            query.append("area[\"name\"=\"").append(sanitizedCity).append("\"](area.countryArea)->.searchArea;\n");
        } else {
            query.append("area[\"name\"=\"").append(sanitizedCity).append("\"]->.searchArea;\n");
        }

        query.append("(\n");
        query.append("  node[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"](area.searchArea);\n");
        query.append("  way[\"amenity\"=\"place_of_worship\"][\"religion\"=\"muslim\"](area.searchArea);\n");
        query.append(");\n");
        query.append("out center tags;\n");

        return executeOverpassQuery(query.toString());
    }

    /**
     * Queries OpenStreetMap for all mosques within a given radius around a center coordinate.
     *
     * @param latitude Center latitude
     * @param longitude Center longitude
     * @param radiusMeters Radius in meters
     * @return Deserialized OSM response
     */
    public OsmResponseDto fetchByRadius(double latitude, double longitude, double radiusMeters) {
        String query = String.format(
                """
                [out:json][timeout:45];
                (
                  node["amenity"="place_of_worship"]["religion"="muslim"](around:%.1f,%.6f,%.6f);
                  way["amenity"="place_of_worship"]["religion"="muslim"](around:%.1f,%.6f,%.6f);
                );
                out center tags;
                """,
                radiusMeters, latitude, longitude,
                radiusMeters, latitude, longitude
        );
        return executeOverpassQuery(query);
    }

    /**
     * Queries OpenStreetMap for all mosques within a rectangular bounding box.
     *
     * @param south Minimum latitude
     * @param west Minimum longitude
     * @param north Maximum latitude
     * @param east Maximum longitude
     * @return Deserialized OSM response
     */
    public OsmResponseDto fetchByBoundingBox(double south, double west, double north, double east) {
        String query = String.format(
                """
                [out:json][timeout:45];
                (
                  node["amenity"="place_of_worship"]["religion"="muslim"](%.6f,%.6f,%.6f,%.6f);
                  way["amenity"="place_of_worship"]["religion"="muslim"](%.6f,%.6f,%.6f,%.6f);
                );
                out center tags;
                """,
                south, west, north, east,
                south, west, north, east
        );
        return executeOverpassQuery(query);
    }

    /**
     * Sends the Overpass QL query string via HTTP POST to the Overpass interpreter endpoint.
     *
     * @param overpassQl The raw Overpass QL script
     * @return Deserialized OsmResponseDto
     */
    public OsmResponseDto executeOverpassQuery(String overpassQl) {
        log.info("Executing Overpass QL query against OSM interpreter:\n{}", overpassQl);

        try {
            OsmResponseDto response = restClient.post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body("data=" + java.net.URLEncoder.encode(overpassQl, java.nio.charset.StandardCharsets.UTF_8))
                    .retrieve()
                    .body(OsmResponseDto.class);

            if (response == null || response.getElements() == null) {
                log.warn("Empty response received from Overpass API");
                return new OsmResponseDto(0.6, "Overpass API", java.util.Collections.emptyList());
            }

            log.info("Successfully fetched {} raw elements from Overpass API", response.getElements().size());
            return response;
        } catch (Exception e) {
            log.error("Failed to query Overpass API: {}", e.getMessage(), e);
            throw new BadRequestException("Overpass API query failed: " + e.getMessage());
        }
    }

    private String escapeOverpassString(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").trim();
    }
}
