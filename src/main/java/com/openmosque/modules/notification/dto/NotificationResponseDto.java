package com.openmosque.modules.notification.dto;

import com.openmosque.modules.notification.entity.NotificationType;
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
public class NotificationResponseDto {
    private UUID id;
    private String title;
    private String message;
    private NotificationType type;
    private String linkUrl;
    private String metadataJson;
    private boolean read;
    private Instant createdAt;
}
