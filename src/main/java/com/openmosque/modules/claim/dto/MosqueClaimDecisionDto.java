package com.openmosque.modules.claim.dto;

import com.openmosque.modules.claim.entity.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueClaimDecisionDto {

    @NotNull(message = "Decision status is required (APPROVED or REJECTED)")
    private ClaimStatus status;

    private String reviewComments;
}
