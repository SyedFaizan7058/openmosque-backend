package com.openmosque.modules.claim.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.claim.dto.MosqueClaimDecisionDto;
import com.openmosque.modules.claim.dto.MosqueClaimResponseDto;
import com.openmosque.modules.claim.entity.ClaimStatus;
import com.openmosque.modules.claim.service.MosqueClaimService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for Admins and Moderators to review mosque claim requests.
 */
@RestController
@RequestMapping("/api/v1/admin/mosques/claims")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MODERATOR', 'SUPER_ADMIN')")
@Tag(name = "Mosque Claims Review (Admin)", description = "Endpoints for Moderators and Admins to review and approve mosque claim requests")
public class MosqueClaimAdminController {

    private final MosqueClaimService claimService;

    /**
     * List claim requests by status.
     */
    @GetMapping
    @Operation(summary = "List mosque claim requests", description = "Retrieves claim requests by status (default: PENDING).")
    public ResponseEntity<ApiResponse<PageResponse<MosqueClaimResponseDto>>> getClaims(
            @Parameter(description = "Claim Status (PENDING, APPROVED, REJECTED)")
            @RequestParam(value = "status", defaultValue = "PENDING") ClaimStatus status,

            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        PageResponse<MosqueClaimResponseDto> claims = claimService.getClaimsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(claims, "Claim requests retrieved successfully"));
    }

    /**
     * Approve or reject a claim request.
     */
    @PatchMapping("/{id}/decision")
    @Operation(summary = "Approve or Reject mosque claim", description = "Reviews a mosque claim. On approval, automatically upgrades claimant to MOSQUE_ADMIN.")
    public ResponseEntity<ApiResponse<MosqueClaimResponseDto>> reviewClaim(
            @PathVariable("id") UUID claimId,
            @Valid @RequestBody MosqueClaimDecisionDto decision,
            @CurrentUser User reviewer
    ) {
        MosqueClaimResponseDto result = claimService.reviewClaim(claimId, decision, reviewer);
        return ResponseEntity.ok(ApiResponse.success(result, "Claim decision recorded successfully"));
    }
}
