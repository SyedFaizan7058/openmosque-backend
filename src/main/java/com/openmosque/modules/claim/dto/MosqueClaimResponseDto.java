package com.openmosque.modules.claim.dto;

import com.openmosque.modules.claim.entity.ClaimStatus;
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
public class MosqueClaimResponseDto {
    private UUID id;
    private UUID mosqueId;
    private String mosqueName;
    private UUID claimantId;
    private String claimantEmail;
    private String fullName;
    private String phoneNumber;
    private String officialEmail;
    private String positionInMosque;
    private String proofDocumentUrl;
    private ClaimStatus status;
    private UUID reviewerId;
    private String reviewerName;
    private String reviewComments;
    private Instant reviewedAt;
    private Instant createdAt;
}
