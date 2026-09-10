package com.openmosque.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to publish or schedule a Friday Jumu'ah Khutbah")
public class MosqueKhutbahCreateDto {

    @NotNull(message = "Khutbah date is required")
    @Schema(description = "Date of Friday Jumu'ah", example = "2026-09-11")
    private LocalDate khutbahDate;

    @NotBlank(message = "Khutbah topic is required")
    @Schema(description = "Topic of the Khutbah", example = "Patience and Community Resilience")
    private String topic;

    @NotBlank(message = "Khatib name is required")
    @Schema(description = "Name of the Imam or Guest Khatib", example = "Imam Abdul Rahman")
    private String khatibName;

    @Schema(description = "Batch number (1 for 1st prayer, 2 for 2nd prayer)", example = "1")
    @Builder.Default
    private int batchNumber = 1;

    @NotNull(message = "Khutbah delivery time is required")
    @Schema(description = "Time the Khutbah commences", example = "13:00:00")
    private LocalTime khutbahTime;

    @Schema(description = "Adhaan time", example = "12:45:00")
    private LocalTime adhaanTime;

    @Schema(description = "Congregation prayer Iqamah time", example = "13:30:00")
    private LocalTime iqamahTime;

    @Schema(description = "Language of delivery", example = "English & Arabic")
    @Builder.Default
    private String language = "English";

    @Schema(description = "Live stream broadcast URL (YouTube, Mixlr, etc.)")
    private String streamUrl;

    @Schema(description = "Archive recording URL after Khutbah")
    private String recordingUrl;

    @Schema(description = "Important worshipper notes (e.g. parking guidance)")
    private String notes;
}
