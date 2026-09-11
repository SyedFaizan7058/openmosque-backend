package com.openmosque.modules.notification.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenResponseDto {
    private UUID id;
    private String fcmToken;
    private String deviceType;
    private String deviceName;
    private Instant lastActiveAt;
    private Instant createdAt;
}
