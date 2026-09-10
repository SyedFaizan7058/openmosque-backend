package com.openmosque.modules.mosque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteMosqueResponseDto {
    private UUID mosqueId;
    private String name;
    private String slug;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private String coverImageUrl;
    private boolean verified;
    private List<String> facilityCodes;
    private Instant favoritedAt;
}
