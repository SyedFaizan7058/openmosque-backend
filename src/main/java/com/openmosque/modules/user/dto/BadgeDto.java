package com.openmosque.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeDto {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String iconName;
    private String category;
    private int thresholdPoints;
    private boolean active;
    private Instant createdAt;
}
