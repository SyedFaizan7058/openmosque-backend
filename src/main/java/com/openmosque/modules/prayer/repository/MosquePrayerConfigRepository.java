package com.openmosque.modules.prayer.repository;

import com.openmosque.modules.prayer.entity.MosquePrayerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MosquePrayerConfigRepository extends JpaRepository<MosquePrayerConfig, UUID> {

    Optional<MosquePrayerConfig> findByMosqueIdAndDeletedFalse(UUID mosqueId);

    Optional<MosquePrayerConfig> findByMosqueSlugAndDeletedFalse(String slug);
}
