package com.openmosque.modules.ingestion.service;

import com.openmosque.common.util.GeoUtils;
import com.openmosque.common.util.SlugUtils;
import com.openmosque.modules.ingestion.client.OverpassApiClient;
import com.openmosque.modules.ingestion.dto.*;
import com.openmosque.modules.mosque.entity.Facility;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.entity.MosqueStatus;
import com.openmosque.modules.mosque.repository.FacilityRepository;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <h3>OsmIngestionService</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Orchestrates the automated batch ingestion of mosques from the OpenStreetMap (OSM) Overpass API.
 * Solves the cold-start data challenge by importing hundreds of real mosques across any city, radius,
 * or bounding box. Features an intelligent spatial deduplication engine (<50m proximity check),
 * automatic extraction of Islamic facilities from standard OSM tags, and URL slug normalization.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Injected into {@link com.openmosque.modules.ingestion.controller.OsmIngestionController}.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OsmIngestionService {

    private final OverpassApiClient overpassApiClient;
    private final MosqueRepository mosqueRepository;
    private final FacilityRepository facilityRepository;

    /** Proximity threshold in meters to consider an OSM item as a duplicate of an existing mosque. */
    private static final double DUPLICATE_PROXIMITY_METERS = 50.0;

    /**
     * Ingests mosques located in an administrative city from OpenStreetMap.
     *
     * @param request City ingestion request parameters
     * @param admin Authenticated administrative user executing the ingestion
     * @return Summary metrics report
     */
    @Transactional
    public IngestionSummaryDto ingestByCity(CityIngestRequestDto request, User admin) {
        long startTime = System.currentTimeMillis();
        log.info("Starting OSM ingestion for city: '{}', country: '{}' (dryRun: {})",
                request.getCity(), request.getCountry(), request.isDryRun());

        OsmResponseDto osmResponse = overpassApiClient.fetchByCity(request.getCity(), request.getCountry());
        return processOsmElements(
                osmResponse.getElements(),
                request.getCity(),
                request.getCountry(),
                request.isDryRun(),
                admin,
                startTime
        );
    }

    /**
     * Ingests mosques surrounding a GPS center coordinate within a radius from OpenStreetMap.
     *
     * @param request Radius ingestion request parameters
     * @param admin Authenticated administrative user
     * @return Summary metrics report
     */
    @Transactional
    public IngestionSummaryDto ingestByRadius(RadiusIngestRequestDto request, User admin) {
        long startTime = System.currentTimeMillis();
        log.info("Starting OSM ingestion for center: ({}, {}) with radius: {}m (dryRun: {})",
                request.getLatitude(), request.getLongitude(), request.getRadiusMeters(), request.isDryRun());

        OsmResponseDto osmResponse = overpassApiClient.fetchByRadius(
                request.getLatitude(),
                request.getLongitude(),
                request.getRadiusMeters()
        );

        return processOsmElements(
                osmResponse.getElements(),
                request.getDefaultCity(),
                request.getDefaultCountry(),
                request.isDryRun(),
                admin,
                startTime
        );
    }

    /**
     * Ingests mosques inside a geographic bounding box from OpenStreetMap.
     *
     * @param request Bounding box ingestion request parameters
     * @param admin Authenticated administrative user
     * @return Summary metrics report
     */
    @Transactional
    public IngestionSummaryDto ingestByBoundingBox(BboxIngestRequestDto request, User admin) {
        long startTime = System.currentTimeMillis();
        log.info("Starting OSM ingestion for bbox: [{}, {}, {}, {}] (dryRun: {})",
                request.getSouth(), request.getWest(), request.getNorth(), request.getEast(), request.isDryRun());

        OsmResponseDto osmResponse = overpassApiClient.fetchByBoundingBox(
                request.getSouth(),
                request.getWest(),
                request.getNorth(),
                request.getEast()
        );

        return processOsmElements(
                osmResponse.getElements(),
                request.getDefaultCity(),
                request.getDefaultCountry(),
                request.isDryRun(),
                admin,
                startTime
        );
    }

    /**
     * Core processing loop: parses elements, applies spatial deduplication, maps facilities, and persists.
     */
    private IngestionSummaryDto processOsmElements(
            List<OsmElementDto> elements,
            String defaultCity,
            String defaultCountry,
            boolean dryRun,
            User admin,
            long startTime
    ) {
        int totalFetched = (elements != null) ? elements.size() : 0;
        int insertedCount = 0;
        int duplicatesSkipped = 0;
        int facilitiesAttachedCount = 0;
        List<String> insertedNames = new ArrayList<>();

        if (elements == null || elements.isEmpty()) {
            return IngestionSummaryDto.builder()
                    .totalElementsFetched(0)
                    .mosquesInserted(0)
                    .duplicatesSkipped(0)
                    .facilitiesAttached(0)
                    .dryRun(dryRun)
                    .insertedMosqueNames(Collections.emptyList())
                    .durationMs(System.currentTimeMillis() - startTime)
                    .build();
        }

        // Cache all existing active mosques to run fast in-memory spatial distance calculations
        List<Mosque> existingMosques = mosqueRepository.findByStatusAndDeletedFalse(MosqueStatus.ACTIVE);
        Map<String, Facility> facilityCatalog = loadFacilityCatalog();

        for (OsmElementDto element : elements) {
            Double lat = element.getEffectiveLatitude();
            Double lon = element.getEffectiveLongitude();

            if (lat == null || lon == null) {
                log.debug("Skipping OSM element ID {} - missing coordinates", element.getId());
                continue;
            }

            Map<String, String> tags = (element.getTags() != null) ? element.getTags() : Collections.emptyMap();
            String name = resolveMosqueName(tags, defaultCity, element.getId());

            // --- Spatial & Name Deduplication Check ---
            if (isDuplicate(lat, lon, name, existingMosques)) {
                log.debug("Skipping duplicate mosque '{}' at ({}, {})", name, lat, lon);
                duplicatesSkipped++;
                continue;
            }

            // Extract address and metadata
            String address = resolveAddress(tags, defaultCity);
            String city = tags.getOrDefault("addr:city", defaultCity != null ? defaultCity : "Unknown City");
            String state = tags.getOrDefault("addr:state", tags.get("addr:province"));
            String country = tags.getOrDefault("addr:country", defaultCountry != null ? defaultCountry : "Unknown Country");
            String postalCode = tags.get("addr:postcode");
            String phone = tags.getOrDefault("phone", tags.get("contact:phone"));
            String email = tags.getOrDefault("email", tags.get("contact:email"));
            String website = tags.getOrDefault("website", tags.get("contact:website"));

            // Resolve Islamic Facilities from OSM tags
            List<String> detectedFacilityCodes = extractFacilityCodes(tags);

            if (!dryRun) {
                String slug = SlugUtils.generateUniqueSlug(name, city, mosqueRepository::existsBySlug);

                Mosque mosque = Mosque.builder()
                        .name(name)
                        .slug(slug)
                        .description(String.format("Imported from OpenStreetMap (OSM %s ID: %d). Data © OpenStreetMap contributors.",
                                element.getType(), element.getId()))
                        .address(address)
                        .city(city)
                        .state(state)
                        .country(country)
                        .postalCode(postalCode)
                        .latitude(lat)
                        .longitude(lon)
                        .location(GeoUtils.createPoint(lat, lon))
                        .contactPhone(phone)
                        .contactEmail(email)
                        .websiteUrl(website)
                        .verified(false)
                        .status(MosqueStatus.ACTIVE)
                        .createdBy(admin)
                        .build();

                // Attach facilities
                for (String code : detectedFacilityCodes) {
                    Facility facility = facilityCatalog.get(code);
                    if (facility != null) {
                        mosque.addFacility(facility, "Detected from OpenStreetMap tags");
                        facilitiesAttachedCount++;
                    }
                }

                Mosque saved = mosqueRepository.save(mosque);
                existingMosques.add(saved); // Add to in-memory list to prevent duplicates within the same batch
            } else {
                facilitiesAttachedCount += detectedFacilityCodes.size();
            }

            insertedCount++;
            insertedNames.add(name);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("OSM Ingestion completed in {}ms: {} fetched, {} inserted, {} duplicates skipped, {} facilities attached (dryRun: {})",
                duration, totalFetched, insertedCount, duplicatesSkipped, facilitiesAttachedCount, dryRun);

        return IngestionSummaryDto.builder()
                .totalElementsFetched(totalFetched)
                .mosquesInserted(insertedCount)
                .duplicatesSkipped(duplicatesSkipped)
                .facilitiesAttached(facilitiesAttachedCount)
                .dryRun(dryRun)
                .insertedMosqueNames(insertedNames)
                .durationMs(duration)
                .build();
    }

    /**
     * Checks if a candidate mosque coordinate and name matches any existing mosque in the database.
     * Criteria: Distance <= 50 meters, OR Distance <= 500 meters with identical normalized name.
     */
    private boolean isDuplicate(double lat, double lon, String name, List<Mosque> existingMosques) {
        String normalizedName = SlugUtils.toSlug(name);

        for (Mosque existing : existingMosques) {
            double distanceKm = GeoUtils.calculateDistanceKm(lat, lon, existing.getLatitude(), existing.getLongitude());
            double distanceMeters = distanceKm * 1000.0;

            // Strict spatial threshold: within 50 meters
            if (distanceMeters <= DUPLICATE_PROXIMITY_METERS) {
                return true;
            }

            // Name match threshold: within 500 meters with same normalized name
            if (distanceMeters <= 500.0) {
                String existingNormalized = SlugUtils.toSlug(existing.getName());
                if (normalizedName.equals(existingNormalized)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String resolveMosqueName(Map<String, String> tags, String defaultCity, Long osmId) {
        if (tags.containsKey("name") && !tags.get("name").isBlank()) {
            return tags.get("name").trim();
        }
        if (tags.containsKey("name:en") && !tags.get("name:en").isBlank()) {
            return tags.get("name:en").trim();
        }
        if (tags.containsKey("official_name") && !tags.get("official_name").isBlank()) {
            return tags.get("official_name").trim();
        }
        String fallbackCity = (defaultCity != null && !defaultCity.isBlank()) ? defaultCity : "Mosque";
        return fallbackCity + " Masjid (OSM #" + osmId + ")";
    }

    private String resolveAddress(Map<String, String> tags, String defaultCity) {
        String houseNumber = tags.get("addr:housenumber");
        String street = tags.get("addr:street");

        if (street != null && !street.isBlank()) {
            return (houseNumber != null && !houseNumber.isBlank()) ? houseNumber + " " + street : street;
        }
        if (tags.containsKey("addr:full")) {
            return tags.get("addr:full");
        }
        return defaultCity != null ? defaultCity : "Address unlisted";
    }

    /**
     * Analyzes OSM key-value tags and maps them to standard OpenMosque facility codes.
     */
    private List<String> extractFacilityCodes(Map<String, String> tags) {
        List<String> codes = new ArrayList<>();

        // Women's Prayer Section
        String female = tags.get("female");
        String segregation = tags.get("segregation");
        if ("yes".equalsIgnoreCase(female) || "dedicated".equalsIgnoreCase(female) ||
                "separate".equalsIgnoreCase(female) || "yes".equalsIgnoreCase(segregation)) {
            codes.add("WOMENS_SECTION");
        }

        // Wheelchair Accessibility
        String wheelchair = tags.get("wheelchair");
        if ("yes".equalsIgnoreCase(wheelchair) || "designated".equalsIgnoreCase(wheelchair)) {
            codes.add("WHEELCHAIR_ACCESSIBILITY");
        }

        // Parking
        String parking = tags.get("parking");
        if ("yes".equalsIgnoreCase(parking) || tags.containsKey("parking:fee")) {
            codes.add("PARKING");
        }

        // Wudu Area / Ablution
        String ablution = tags.get("ablution");
        String toilets = tags.get("toilets");
        if ("yes".equalsIgnoreCase(ablution) || "yes".equalsIgnoreCase(toilets) || tags.containsKey("toilets:wheelchair")) {
            codes.add("WUDU_AREA");
        }

        // Air Conditioning
        if ("yes".equalsIgnoreCase(tags.get("air_conditioning"))) {
            codes.add("AIR_CONDITIONING");
        }

        // Islamic Library
        if ("yes".equalsIgnoreCase(tags.get("library"))) {
            codes.add("LIBRARY");
        }

        return codes;
    }

    private Map<String, Facility> loadFacilityCatalog() {
        Map<String, Facility> map = new HashMap<>();
        List<Facility> allFacilities = facilityRepository.findAll();
        for (Facility f : allFacilities) {
            map.put(f.getCode(), f);
        }
        return map;
    }
}
