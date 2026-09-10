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
public class UserBadgeResponseDto {
    private UUID id;
    private UUID badgeId;
    private String code;
    private String name;
    private String description;
    private String iconName;
    private String category;
    private Instant earnedAt;
}
