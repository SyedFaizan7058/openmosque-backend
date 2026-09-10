package com.openmosque.modules.event.repository;

import com.openmosque.modules.event.entity.MosqueKhutbah;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MosqueKhutbahRepository extends JpaRepository<MosqueKhutbah, UUID> {

    @Query("SELECT k FROM MosqueKhutbah k WHERE k.mosque.id = :mosqueId AND k.deleted = false AND k.khutbahDate >= :date ORDER BY k.khutbahDate ASC, k.batchNumber ASC")
    List<MosqueKhutbah> findUpcomingByMosqueId(@Param("mosqueId") UUID mosqueId, @Param("date") LocalDate date);

    @Query("SELECT k FROM MosqueKhutbah k WHERE k.mosque.id = :mosqueId AND k.deleted = false ORDER BY k.khutbahDate DESC, k.batchNumber ASC")
    Page<MosqueKhutbah> findAllByMosqueId(@Param("mosqueId") UUID mosqueId, Pageable pageable);

    @Query("SELECT k FROM MosqueKhutbah k WHERE k.mosque.id = :mosqueId AND k.khutbahDate = :date AND k.deleted = false ORDER BY k.batchNumber ASC")
    List<MosqueKhutbah> findByMosqueIdAndKhutbahDate(@Param("mosqueId") UUID mosqueId, @Param("date") LocalDate date);
}
