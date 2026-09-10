package com.openmosque.modules.mosque.dto;

import com.openmosque.modules.mosque.entity.MosqueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Lightweight DTO for Nearby lists, map markers, and search query results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueSummaryDto {
    private UUID id;
    private String name;
    private String slug;
    private String address;
    private String city;
    private String state;
    private String country;
    private double latitude;
    private double longitude;
    private Double distanceKm;
    private String coverImageUrl;
    private boolean verified;
    private MosqueStatus status;
    private String liveStreamUrl;
    private List<String> facilityCodes;
    private Double rating;
    private Integer reviewCount;
}
