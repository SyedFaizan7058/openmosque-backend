package com.openmosque.modules.user.dto;

import com.openmosque.modules.user.entity.UserRole;
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
public class UserResponseDto {
    private UUID id;
    private String firebaseUid;
    private String email;
    private String displayName;
    private String phoneNumber;
    private String photoUrl;
    private UserRole role;
    private int points;
    private boolean active;
    private boolean verified;
    private String preferredCity;
    private String preferredCountry;
    private Double latitude;
    private Double longitude;
    private java.util.List<UUID> claimedMosqueIds;
    private Instant createdAt;
}
