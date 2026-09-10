package com.openmosque.modules.claim.service;

import com.openmosque.common.exception.BadRequestException;
import com.openmosque.common.exception.ConflictException;
import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.claim.dto.MosqueClaimDecisionDto;
import com.openmosque.modules.claim.dto.MosqueClaimResponseDto;
import com.openmosque.modules.claim.dto.MosqueClaimSubmitDto;
import com.openmosque.modules.claim.entity.ClaimStatus;
import com.openmosque.modules.claim.entity.MosqueClaimRequest;
import com.openmosque.modules.claim.mapper.MosqueClaimMapper;
import com.openmosque.modules.claim.repository.MosqueClaimRequestRepository;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service managing Mosque Administrator Claim & Verification requests (Way B).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MosqueClaimService {

    private final MosqueClaimRequestRepository claimRepository;
    private final MosqueRepository mosqueRepository;
    private final UserRepository userRepository;
    private final MosqueClaimMapper claimMapper;
    private final com.openmosque.modules.user.service.BadgeService badgeService;
    private final com.openmosque.modules.notification.service.NotificationService notificationService;

    /**
     * Submits a new claim request for administrative ownership of a mosque.
     */
    @Transactional
    public MosqueClaimResponseDto submitClaim(UUID mosqueId, MosqueClaimSubmitDto dto, User claimant) {
        Mosque mosque = mosqueRepository.findByIdAndDeletedFalse(mosqueId)
                .orElseThrow(() -> new ResourceNotFoundException("Mosque", "id", mosqueId));

        // Check for duplicate pending claim
        claimRepository.findByMosqueIdAndClaimantIdAndStatus(mosqueId, claimant.getId(), ClaimStatus.PENDING)
                .ifPresent(existing -> {
                    throw new ConflictException("You already have a pending claim request for this mosque");
                });

        MosqueClaimRequest request = claimMapper.toEntity(dto);
        request.setMosque(mosque);
        request.setClaimant(claimant);
        request.setStatus(ClaimStatus.PENDING);

        MosqueClaimRequest saved = claimRepository.save(request);
        log.info("User '{}' submitted claim request for mosque '{}' (Claim ID: {})",
                claimant.getEmail(), mosque.getName(), saved.getId());

        return claimMapper.toDto(saved);
    }

    /**
     * Retrieves paginated claim requests filtered by status.
     */
    @Transactional(readOnly = true)
    public PageResponse<MosqueClaimResponseDto> getClaimsByStatus(ClaimStatus status, Pageable pageable) {
        Page<MosqueClaimRequest> page = claimRepository.findByStatusAndDeletedFalse(status, pageable);
        List<MosqueClaimResponseDto> dtoList = claimMapper.toDtoList(page.getContent());
        return PageResponse.from(page, dtoList);
    }

    /**
     * Reviews and approves/rejects a mosque claim request.
     * On APPROVED: upgrades user to MOSQUE_ADMIN and marks mosque as verified.
     */
    @Transactional
    public MosqueClaimResponseDto reviewClaim(UUID claimId, MosqueClaimDecisionDto decision, User reviewer) {
        MosqueClaimRequest claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("MosqueClaimRequest", "id", claimId));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("Claim request has already been reviewed with status: " + claim.getStatus());
        }

        claim.setStatus(decision.getStatus());
        claim.setReviewer(reviewer);
        claim.setReviewComments(decision.getReviewComments());
        claim.setReviewedAt(Instant.now());

        if (decision.getStatus() == ClaimStatus.APPROVED) {
            // 1. Upgrade user's role to MOSQUE_ADMIN
            User claimant = claim.getClaimant();
            if (claimant.getRole() == UserRole.USER) {
                claimant.setRole(UserRole.MOSQUE_ADMIN);
                claimant.setVerified(true);
                userRepository.save(claimant);
                log.info("Upgraded user '{}' to MOSQUE_ADMIN upon claim approval", claimant.getEmail());
            }
            badgeService.awardBadge(claimant, com.openmosque.modules.user.service.BadgeService.BADGE_VERIFIED_IMAM);

            // 2. Mark mosque as verified
            Mosque mosque = claim.getMosque();
            mosque.setVerified(true);
            mosqueRepository.save(mosque);
            log.info("Marked mosque '{}' as verified", mosque.getName());

            notificationService.notifyUser(
                    claimant,
                    "Mosque Claim Approved: " + mosque.getName(),
                    "Your administrative claim has been verified! You now have MOSQUE_ADMIN privileges to update Iqamah schedules and events.",
                    com.openmosque.modules.notification.entity.NotificationType.CLAIM_APPROVED,
                    "/mosques/" + mosque.getSlug(),
                    null
            );
        } else if (decision.getStatus() == ClaimStatus.REJECTED) {
            notificationService.notifyUser(
                    claim.getClaimant(),
                    "Mosque Claim Not Approved",
                    "Your administrative claim for '" + claim.getMosque().getName() + "' was not approved. Comments: " +
                            (decision.getReviewComments() != null ? decision.getReviewComments() : "Verification documents could not be validated."),
                    com.openmosque.modules.notification.entity.NotificationType.CLAIM_REJECTED,
                    null,
                    null
            );
        }

        MosqueClaimRequest updated = claimRepository.save(claim);
        return claimMapper.toDto(updated);
    }
}
