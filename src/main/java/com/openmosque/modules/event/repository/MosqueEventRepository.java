package com.openmosque.modules.event.repository;

import com.openmosque.modules.event.entity.EventType;
import com.openmosque.modules.event.entity.MosqueEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface MosqueEventRepository extends JpaRepository<MosqueEvent, UUID> {

    @Query("SELECT e FROM MosqueEvent e WHERE e.mosque.id = :mosqueId AND e.deleted = false AND e.endDateTime >= :now ORDER BY e.startDateTime ASC")
    List<MosqueEvent> findUpcomingEventsByMosqueId(@Param("mosqueId") UUID mosqueId, @Param("now") Instant now);

    @Query("SELECT e FROM MosqueEvent e WHERE e.mosque.id = :mosqueId AND e.deleted = false " +
           "AND (:eventType IS NULL OR e.eventType = :eventType) " +
           "AND e.endDateTime >= :now ORDER BY e.startDateTime ASC")
    Page<MosqueEvent> findUpcomingEventsByMosqueIdFiltered(
            @Param("mosqueId") UUID mosqueId,
            @Param("eventType") EventType eventType,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query("SELECT e FROM MosqueEvent e WHERE e.mosque.id = :mosqueId AND e.deleted = false ORDER BY e.startDateTime DESC")
    Page<MosqueEvent> findAllByMosqueId(@Param("mosqueId") UUID mosqueId, Pageable pageable);

    @Query("SELECT e FROM MosqueEvent e WHERE e.mosque.id = :mosqueId " +
           "AND e.deleted = false AND e.cancelled = false " +
           "AND (:excludeEventId IS NULL OR e.id != :excludeEventId) " +
           "AND e.startDateTime < :endDateTime " +
           "AND e.endDateTime > :startDateTime " +
           "ORDER BY e.startDateTime ASC")
    List<MosqueEvent> findConflictingEvents(
            @Param("mosqueId") UUID mosqueId,
            @Param("startDateTime") Instant startDateTime,
            @Param("endDateTime") Instant endDateTime,
            @Param("excludeEventId") UUID excludeEventId
    );
}
