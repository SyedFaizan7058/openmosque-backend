package com.openmosque.modules.claim.repository;

import com.openmosque.modules.claim.entity.ClaimStatus;
import com.openmosque.modules.claim.entity.MosqueClaimRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MosqueClaimRequestRepository extends JpaRepository<MosqueClaimRequest, UUID> {

    Page<MosqueClaimRequest> findByStatusAndDeletedFalse(ClaimStatus status, Pageable pageable);

    Optional<MosqueClaimRequest> findByMosqueIdAndClaimantIdAndStatus(
            UUID mosqueId, UUID claimantId, ClaimStatus status);

    boolean existsByMosqueIdAndClaimantIdAndStatus(UUID mosqueId, UUID claimantId, ClaimStatus status);

    java.util.List<MosqueClaimRequest> findByClaimantIdAndStatus(UUID claimantId, ClaimStatus status);

    long countByStatus(ClaimStatus status);
}
