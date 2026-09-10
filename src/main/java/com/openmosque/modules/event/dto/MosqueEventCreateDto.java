package com.openmosque.modules.event.dto;

import com.openmosque.modules.event.entity.EventAudience;
import com.openmosque.modules.event.entity.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to create or update a mosque event")
public class MosqueEventCreateDto {

    @NotBlank(message = "Event title is required")
    @Schema(description = "Event title", example = "Ramadan Tafseer Halaqah")
    private String title;

    @Schema(description = "Full event description")
    private String description;

    @NotNull(message = "Event type is required")
    @Schema(description = "Category of event", example = "HALAQAH")
    private EventType eventType;

    @Schema(description = "Intended audience", example = "ALL")
    @Builder.Default
    private EventAudience audience = EventAudience.ALL;

    @NotNull(message = "Start date time is required")
    @Schema(description = "Event start timestamp (ISO-8601 UTC)")
    private Instant startDateTime;

    @NotNull(message = "End date time is required")
    @Schema(description = "Event end timestamp (ISO-8601 UTC)")
    private Instant endDateTime;

    @Schema(description = "Specific hall or location", example = "Main Prayer Hall")
    private String locationDetails;

    @Schema(description = "Speaker / Instructor name", example = "Shaykh Ahmad")
    private String speakerName;

    @Schema(description = "Cover banner image URL")
    private String bannerImageUrl;

    @Schema(description = "Optional external registration link")
    private String registrationUrl;
}
