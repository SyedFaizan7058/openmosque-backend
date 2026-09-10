package com.openmosque.modules.event.dto;

import com.openmosque.modules.event.entity.EventAudience;
import com.openmosque.modules.event.entity.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Details of a scheduled mosque event")
public class MosqueEventResponseDto {

    private UUID id;
    private UUID mosqueId;
    private String mosqueName;
    private String title;
    private String description;
    private EventType eventType;
    private EventAudience audience;
    private Instant startDateTime;
    private Instant endDateTime;
    private String locationDetails;
    private String speakerName;
    private String bannerImageUrl;
    private String registrationUrl;
    private boolean cancelled;
    private Instant createdAt;
}
