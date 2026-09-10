package com.openmosque.modules.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Details of a Friday Jumu'ah Khutbah")
public class MosqueKhutbahResponseDto {

    private UUID id;
    private UUID mosqueId;
    private String mosqueName;
    private LocalDate khutbahDate;
    private String topic;
    private String khatibName;
    private int batchNumber;
    private LocalTime khutbahTime;
    private LocalTime adhaanTime;
    private LocalTime iqamahTime;
    private String language;
    private String streamUrl;
    private String recordingUrl;
    private String notes;
}
