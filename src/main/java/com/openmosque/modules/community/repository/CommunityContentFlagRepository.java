package com.openmosque.modules.community.repository;

import com.openmosque.modules.community.entity.CommunityContentFlag;
import com.openmosque.modules.community.entity.FlagStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommunityContentFlagRepository extends JpaRepository<CommunityContentFlag, UUID> {

    Page<CommunityContentFlag> findByStatusAndDeletedFalseOrderByCreatedAtDesc(FlagStatus status, Pageable pageable);

    long countByStatus(FlagStatus status);
}
