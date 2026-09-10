package com.openmosque.modules.mosque.repository;

import com.openmosque.modules.mosque.entity.MosqueImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MosqueImageRepository extends JpaRepository<MosqueImage, UUID> {
    List<MosqueImage> findByMosqueIdAndDeletedFalseOrderByDisplayOrderAsc(UUID mosqueId);
}
