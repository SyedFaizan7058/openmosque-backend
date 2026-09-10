package com.openmosque.modules.claim.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.claim.dto.MosqueClaimResponseDto;
import com.openmosque.modules.claim.dto.MosqueClaimSubmitDto;
import com.openmosque.modules.claim.service.MosqueClaimService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for Committee Members & Imams to claim administrative control over a mosque.
 */
@RestController
@RequestMapping("/api/v1/mosques")
@RequiredArgsConstructor
@Tag(name = "Mosque Claiming & Admin Access", description = "Endpoints for Imams and Committee members to request administrative access")
public class MosqueClaimController {

    private final MosqueClaimService claimService;

    /**
     * Apply to claim a mosque as an administrator.
     */
    @PostMapping("/{id}/claim")
    @Operation(summary = "Submit mosque claim request", description = "Allows a committee member/imam to apply to manage their mosque. On admin approval, their role is upgraded to MOSQUE_ADMIN.")
    public ResponseEntity<ApiResponse<MosqueClaimResponseDto>> submitClaim(
            @PathVariable("id") UUID mosqueId,
            @Valid @RequestBody MosqueClaimSubmitDto request,
            @CurrentUser User claimant
    ) {
        MosqueClaimResponseDto response = claimService.submitClaim(mosqueId, request, claimant);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Claim request submitted successfully for admin review"));
    }
}
