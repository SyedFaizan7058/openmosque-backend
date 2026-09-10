package com.openmosque.modules.moderation.dto;

import com.openmosque.modules.moderation.entity.SubmissionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload sent by Moderator when approving or rejecting a mosque submission.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDecisionDto {

    @NotNull(message = "Decision status is required (APPROVED or REJECTED)")
    private SubmissionStatus status;

    /**
     * Feedback or justification comments from the moderator.
     */
    private String reviewComments;
}
