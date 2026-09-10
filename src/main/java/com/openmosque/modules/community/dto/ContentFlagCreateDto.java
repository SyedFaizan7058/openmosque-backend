package com.openmosque.modules.community.dto;

import com.openmosque.modules.community.entity.TargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to report/flag inappropriate community content")
public class ContentFlagCreateDto {

    @NotNull(message = "Target type is required")
    @Schema(description = "Type of content being reported", example = "REVIEW")
    private TargetType targetType;

    @NotNull(message = "Target ID is required")
    @Schema(description = "UUID of the review, question, or answer being reported")
    private UUID targetId;

    @NotBlank(message = "Reason is required")
    @Schema(description = "Explanation of why content violates community standards", example = "Spam links and inappropriate language")
    private String reason;
}
