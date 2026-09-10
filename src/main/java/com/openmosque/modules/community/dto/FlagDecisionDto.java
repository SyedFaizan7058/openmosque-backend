package com.openmosque.modules.community.dto;

import com.openmosque.modules.community.entity.FlagStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Moderator decision on a flagged piece of community content")
public class FlagDecisionDto {

    @NotNull(message = "Decision status is required")
    @Schema(description = "Decision: RESOLVED (hides inappropriate content) or DISMISSED (keeps content published)", example = "RESOLVED")
    private FlagStatus status;

    @Schema(description = "Moderator notes or audit rationale", example = "Review violates community guidelines. Content hidden.")
    private String reviewerNotes;
}
