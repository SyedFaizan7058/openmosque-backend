package com.openmosque.modules.community.repository;

import com.openmosque.modules.community.entity.ContentStatus;
import com.openmosque.modules.community.entity.MosqueReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MosqueReviewRepository extends JpaRepository<MosqueReview, UUID> {

    Page<MosqueReview> findByMosqueIdAndStatusAndDeletedFalse(UUID mosqueId, ContentStatus status, Pageable pageable);

    Optional<MosqueReview> findByMosqueIdAndUserIdAndDeletedFalse(UUID mosqueId, UUID userId);

    @Query("SELECT COUNT(r), AVG(r.ratingOverall), AVG(r.ratingCleanliness), AVG(r.ratingFacilities), AVG(r.ratingWomensArea), AVG(r.ratingParking) " +
           "FROM MosqueReview r WHERE r.mosque.id = :mosqueId AND r.status = 'PUBLISHED' AND r.deleted = false")
    Object[] getRatingSummaryByMosqueId(@Param("mosqueId") UUID mosqueId);

    @Query("SELECT r.mosque.id, COUNT(r), AVG(r.ratingOverall) " +
           "FROM MosqueReview r WHERE r.status = 'PUBLISHED' AND r.deleted = false GROUP BY r.mosque.id")
    java.util.List<Object[]> getAllMosqueRatingSummaries();
}
