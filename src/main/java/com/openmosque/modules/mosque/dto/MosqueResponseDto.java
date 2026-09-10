package com.openmosque.modules.mosque.dto;

import com.openmosque.modules.mosque.entity.MosqueStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full Mosque Profile Response DTO including all facilities, contact metadata, and image gallery.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueResponseDto {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private double latitude;
    private double longitude;
    private String contactPhone;
    private String contactEmail;
    private String websiteUrl;
    private String liveStreamUrl;
    private boolean verified;
    private MosqueStatus status;
    private List<MosqueFacilityDto> facilities;
    private List<MosqueImageDto> images;
    private Instant createdAt;
    private Double rating;
    private Integer reviewCount;
}
