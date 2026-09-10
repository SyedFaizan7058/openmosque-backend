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
public class FacilityDto {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String iconName;
    private boolean active;
}
