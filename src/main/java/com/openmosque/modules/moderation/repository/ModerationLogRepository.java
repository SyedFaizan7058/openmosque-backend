package com.openmosque.modules.moderation.repository;

import com.openmosque.modules.moderation.entity.ModerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModerationLogRepository extends JpaRepository<ModerationLog, UUID> {
    List<ModerationLog> findBySubmissionIdOrderByCreatedAtDesc(UUID submissionId);
}
