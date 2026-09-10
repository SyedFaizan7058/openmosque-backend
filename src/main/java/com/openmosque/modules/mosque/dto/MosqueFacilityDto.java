package com.openmosque.modules.mosque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueFacilityDto {
    private UUID id;
    private UUID facilityId;
    private String facilityCode;
    private String facilityName;
    private String iconName;
    private String customDetails;
}
