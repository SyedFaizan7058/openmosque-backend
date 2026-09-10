package com.openmosque.modules.claim.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueClaimSubmitDto {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Official email is required")
    @jakarta.validation.constraints.Email(message = "A valid official email is required")
    private String officialEmail;

    @NotBlank(message = "Position in mosque is required (e.g. Imam, Trustee, Secretary)")
    private String positionInMosque;

    @NotBlank(message = "Proof document (charity certificate or ID) is required")
    private String proofDocumentUrl;
}
