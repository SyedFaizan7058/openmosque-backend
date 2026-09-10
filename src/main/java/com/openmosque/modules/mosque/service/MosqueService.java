package com.openmosque.modules.mosque.service;

import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.common.model.PageResponse;
import com.openmosque.common.util.GeoUtils;
import com.openmosque.common.util.SlugUtils;
import com.openmosque.modules.mosque.dto.MosqueCreateRequestDto;
import com.openmosque.modules.mosque.dto.MosqueResponseDto;
import com.openmosque.modules.mosque.dto.MosqueSummaryDto;
import com.openmosque.modules.mosque.entity.Facility;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.entity.MosqueStatus;
import com.openmosque.modules.mosque.mapper.MosqueMapper;
import com.openmosque.modules.mosque.repository.FacilityRepository;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service handling Mosque Directory, PostGIS Spatial Radius Searches, and Profile management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MosqueService {

    private final MosqueRepository mosqueRepository;
    private final FacilityRepository facilityRepository;
    private final MosqueMapper mosqueMapper;
    private final com.openmosque.modules.community.repository.MosqueReviewRepository reviewRepository;
    private final com.openmosque.modules.claim.repository.MosqueClaimRequestRepository claimRepository;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    /**
     * Finds active mosques within a radius (in kilometers) from given latitude and longitude.
     * Uses PostGIS ST_DWithin and ST_DistanceSphere for fast spatial indexing in PostgreSQL,
     * with high-performance Haversine calculation for test environments.
     * 
     * @param latitude User GPS Latitude
     * @param longitude User GPS Longitude
     * @param radiusKm Radius in kilometers (default 10km)
     * @param requiredFacilityCodes Optional facility filters (e.g. ['WOMENS_SECTION', 'PARKING'])
     * @return List of nearby MosqueSummaryDto ordered by closest distance
     */
    @Transactional(readOnly = true)
    public List<MosqueSummaryDto> getNearbyMosques(
            double latitude,
            double longitude,
            double radiusKm,
            List<String> requiredFacilityCodes
    ) {
        GeoUtils.validateCoordinates(latitude, longitude);
        double radiusMeters = radiusKm * 1000.0;

        List<Mosque> mosques;
        boolean isPostgreSql = datasourceUrl != null && datasourceUrl.contains(":postgresql:");

        if (isPostgreSql) {
            try {
                mosques = mosqueRepository.findNearbyMosques(latitude, longitude, radiusMeters);
            } catch (Exception e) {
                log.warn("PostGIS query error: {}. Falling back to standard query.", e.getMessage());
                mosques = findNearbyFallback(latitude, longitude, radiusKm);
            }
        } else {
            // H2 / In-memory test environment fallback
            mosques = findNearbyFallback(latitude, longitude, radiusKm);
        }

        // Apply facility filtering if requested
        if (requiredFacilityCodes != null && !requiredFacilityCodes.isEmpty()) {
            Set<String> requiredSet = requiredFacilityCodes.stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet());

            mosques = mosques.stream()
                    .filter(m -> {
                        Set<String> mosqueFacilityCodes = m.getFacilities().stream()
                                .map(mf -> mf.getFacility().getCode().toUpperCase())
                                .collect(Collectors.toSet());
                        return mosqueFacilityCodes.containsAll(requiredSet);
                    })
                    .toList();
        }

        // Map to summary DTO with calculated spherical distance in kilometers
        List<MosqueSummaryDto> dtoList = mosques.stream().map(m -> {
            MosqueSummaryDto dto = mosqueMapper.toSummaryDto(m);
            double distance = GeoUtils.calculateDistanceKm(latitude, longitude, m.getLatitude(), m.getLongitude());
            dto.setDistanceKm(distance);
            return dto;
        }).toList();

        enrichSummaryListWithRatings(dtoList);
        return dtoList;
    }

    private List<Mosque> findNearbyFallback(double latitude, double longitude, double radiusKm) {
        return mosqueRepository.findByStatusAndDeletedFalse(MosqueStatus.ACTIVE).stream()
                .filter(m -> GeoUtils.calculateDistanceKm(latitude, longitude, m.getLatitude(), m.getLongitude()) <= radiusKm)
                .sorted(Comparator.comparingDouble(m ->
                        GeoUtils.calculateDistanceKm(latitude, longitude, m.getLatitude(), m.getLongitude())))
                .toList();
    }

    /**
     * Filtered search with pagination across name, city, and country.
     */
    @Transactional(readOnly = true)
    public PageResponse<MosqueSummaryDto> searchMosques(
            String query,
            String city,
            String country,
            Pageable pageable
    ) {
        Page<Mosque> page = mosqueRepository.searchMosques(
                query, city, country, MosqueStatus.ACTIVE, pageable);

        List<MosqueSummaryDto> summaryList = page.getContent().stream()
                .map(mosqueMapper::toSummaryDto)
                .toList();

        enrichSummaryListWithRatings(summaryList);
        return PageResponse.from(page, summaryList);
    }

    /**
     * Resolves a persistent Mosque entity by either its UUID string or its unique slug.
     *
     * WHY THIS IS WRITTEN:
     * Eliminates code duplication across modules (Prayer, Events, Reviews, Q&A)
     * by providing a single, robust lookup method that handles UUID vs slug format detection.
     *
     * WHERE IT IS USED:
     * - MosqueService.getMosqueByIdOrSlug (public profile)
     * - PrayerTimesService (Adhan times & Iqamah calculations)
     * - MosqueEventService (Mosque events listing)
     * - MosqueKhutbahService (Friday khutbah schedules)
     * - CommunityReviewService (Review submission & listing)
     * - CommunityQAService (Q&A thread retrieval)
     *
     * @param identifier UUID string or unique slug (e.g. "east-london-mosque-london")
     * @return Active, non-deleted Mosque entity
     * @throws ResourceNotFoundException if mosque is not found
     */
    @Transactional(readOnly = true)
    public Mosque findEntityByIdOrSlug(String identifier) {
        try {
            UUID id = UUID.fromString(identifier);
            return mosqueRepository.findByIdAndDeletedFalse(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Mosque", "id", identifier));
        } catch (IllegalArgumentException e) {
            return mosqueRepository.findBySlugAndDeletedFalse(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("Mosque", "slug", identifier));
        }
    }

    /**
     * Retrieves full mosque profile DTO by UUID string or unique slug.
     * Used by public profile screens in web and mobile applications.
     */
    @Transactional(readOnly = true)
    public MosqueResponseDto getMosqueByIdOrSlug(String identifier) {
        MosqueResponseDto dto = mosqueMapper.toResponseDto(findEntityByIdOrSlug(identifier));
        enrichWithRatings(dto);
        return dto;
    }

    /**
     * Updates an existing Mosque record.
     * Enforces that the user is a SUPER_ADMIN or an approved MOSQUE_ADMIN for this mosque.
     */
    @Transactional
    public MosqueResponseDto updateMosque(UUID mosqueId, com.openmosque.modules.mosque.dto.MosqueUpdateRequestDto request, User user) {
        boolean isApprovedAdmin = false;
        if (user.getRole() == com.openmosque.modules.user.entity.UserRole.SUPER_ADMIN) {
            isApprovedAdmin = true;
        } else if (user.getRole() == com.openmosque.modules.user.entity.UserRole.MOSQUE_ADMIN) {
            isApprovedAdmin = claimRepository.findByMosqueIdAndClaimantIdAndStatus(
                    mosqueId, user.getId(), com.openmosque.modules.claim.entity.ClaimStatus.APPROVED).isPresent();
        }

        if (!isApprovedAdmin) {
            throw new com.openmosque.common.exception.ForbiddenException(
                    "You do not have permission to update this mosque. Only verified Mosque Administrators or Super Admins are permitted.");
        }

        Mosque mosque = mosqueRepository.findByIdAndDeletedFalse(mosqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Mosque", "id", mosqueId));

        mosque.setName(request.getName());
        mosque.setDescription(request.getDescription());
        mosque.setAddress(request.getAddress());
        mosque.setCity(request.getCity());
        mosque.setState(request.getState());
        mosque.setCountry(request.getCountry());
        mosque.setPostalCode(request.getPostalCode());

        if (request.getLatitude() != 0 && request.getLongitude() != 0) {
            mosque.setLatitude(request.getLatitude());
            mosque.setLongitude(request.getLongitude());
            mosque.setLocation(GeoUtils.createPoint(request.getLatitude(), request.getLongitude()));
        }

        mosque.setContactPhone(request.getContactPhone());
        mosque.setContactEmail(request.getContactEmail());
        mosque.setWebsiteUrl(request.getWebsiteUrl());
        mosque.setLiveStreamUrl(request.getLiveStreamUrl());

        if (request.getFacilityCodes() != null) {
            List<String> targetCodes = request.getFacilityCodes();
            // Remove facilities no longer selected
            mosque.getFacilities().removeIf(mf -> mf.getFacility() != null && !targetCodes.contains(mf.getFacility().getCode()));
            // Find which codes are already present
            java.util.Set<String> existingCodes = mosque.getFacilities().stream()
                    .filter(mf -> mf.getFacility() != null)
                    .map(mf -> mf.getFacility().getCode())
                    .collect(java.util.stream.Collectors.toSet());
            // Add new ones
            List<String> toAdd = targetCodes.stream().filter(c -> !existingCodes.contains(c)).toList();
            if (!toAdd.isEmpty()) {
                List<Facility> facilities = facilityRepository.findByCodeIn(toAdd);
                facilities.forEach(facility -> mosque.addFacility(facility, null));
            }
        }

        Mosque saved = mosqueRepository.saveAndFlush(mosque);
        log.info("Mosque '{}' (ID: {}) updated by user '{}'", saved.getName(), saved.getId(), user.getEmail());
        MosqueResponseDto response = mosqueMapper.toResponseDto(saved);
        enrichWithRatings(response);
        return response;
    }

    /**
     * Creates and publishes a new Mosque record.
     */
    @Transactional
    public MosqueResponseDto createMosque(MosqueCreateRequestDto request, User creator) {
        Point point = GeoUtils.createPoint(request.getLatitude(), request.getLongitude());
        String slug = generateUniqueSlug(request.getName(), request.getCity());

        Mosque mosque = mosqueMapper.toEntity(request);
        mosque.setSlug(slug);
        mosque.setLocation(point);
        mosque.setStatus(MosqueStatus.ACTIVE);
        mosque.setVerified(false);
        mosque.setCreatedBy(creator);

        // Attach facilities
        if (request.getFacilityCodes() != null && !request.getFacilityCodes().isEmpty()) {
            List<Facility> facilities = facilityRepository.findByCodeIn(request.getFacilityCodes());
            facilities.forEach(facility -> mosque.addFacility(facility, null));
        }

        // Attach images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                boolean isCover = (i == 0);
                mosque.addImage(request.getImageUrls().get(i), "Mosque Photo " + (i + 1), isCover, i);
            }
        }

        Mosque savedMosque = mosqueRepository.save(mosque);
        log.info("Mosque created successfully: '{}' (ID: {})", savedMosque.getName(), savedMosque.getId());
        MosqueResponseDto response = mosqueMapper.toResponseDto(savedMosque);
        enrichWithRatings(response);
        return response;
    }

    /**
     * Generates a clean URL-friendly unique slug from name and city.
     * e.g. "East London Mosque", "London" -> "east-london-mosque-london"
     */
    private String generateUniqueSlug(String name, String city) {
        return SlugUtils.generateUniqueSlug(name, city, mosqueRepository::existsBySlug);
    }

    private void enrichWithRatings(MosqueResponseDto dto) {
        if (dto == null || dto.getId() == null) return;
        try {
            Object[] summary = reviewRepository.getRatingSummaryByMosqueId(dto.getId());
            if (summary != null && summary.length > 0 && summary[0] != null) {
                Object[] cols = (summary[0] instanceof Object[]) ? (Object[]) summary[0] : summary;
                long count = (cols[0] != null) ? ((Number) cols[0]).longValue() : 0L;
                Double avg = (cols.length > 1 && cols[1] != null) ? ((Number) cols[1]).doubleValue() : null;
                dto.setReviewCount((int) count);
                dto.setRating(avg != null ? Math.round(avg * 10.0) / 10.0 : null);
            } else {
                dto.setReviewCount(0);
                dto.setRating(null);
            }
        } catch (Exception e) {
            log.warn("Rating enrichment failed for mosque {}: {}", dto.getId(), e.getMessage());
        }
    }

    private void enrichSummaryListWithRatings(List<MosqueSummaryDto> list) {
        if (list == null || list.isEmpty()) return;
        Map<UUID, Object[]> ratingsMap = new HashMap<>();
        try {
            List<Object[]> batch = reviewRepository.getAllMosqueRatingSummaries();
            if (batch != null) {
                for (Object[] row : batch) {
                    if (row != null && row.length >= 3 && row[0] instanceof UUID) {
                        ratingsMap.put((UUID) row[0], row);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Batch rating lookup failed: {}", e.getMessage());
        }

        for (MosqueSummaryDto dto : list) {
            Object[] row = ratingsMap.get(dto.getId());
            if (row != null) {
                long count = row[1] != null ? ((Number) row[1]).longValue() : 0;
                Double avg = row[2] != null ? ((Number) row[2]).doubleValue() : null;
                dto.setReviewCount((int) count);
                dto.setRating(avg != null ? Math.round(avg * 10.0) / 10.0 : null);
            } else {
                dto.setReviewCount(0);
                dto.setRating(null);
            }
        }
    }
}
