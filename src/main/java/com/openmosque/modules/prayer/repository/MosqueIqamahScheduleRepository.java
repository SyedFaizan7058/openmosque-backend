package com.openmosque.modules.prayer.repository;

import com.openmosque.modules.prayer.entity.MosqueIqamahSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MosqueIqamahScheduleRepository extends JpaRepository<MosqueIqamahSchedule, UUID> {

    Optional<MosqueIqamahSchedule> findByMosqueIdAndDeletedFalse(UUID mosqueId);

    Optional<MosqueIqamahSchedule> findByMosqueSlugAndDeletedFalse(String slug);
}
