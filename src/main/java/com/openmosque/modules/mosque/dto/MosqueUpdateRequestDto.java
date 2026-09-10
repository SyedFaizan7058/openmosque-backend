package com.openmosque.modules.mosque.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request payload for Mosque Administrators and Super Admins to update mosque details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueUpdateRequestDto {

    @NotBlank(message = "Mosque name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    @NotBlank(message = "Country is required")
    private String country;

    private String postalCode;

    private double latitude;

    private double longitude;

    private String contactPhone;

    private String contactEmail;

    private String websiteUrl;

    private String liveStreamUrl;

    private List<String> facilityCodes;
}
