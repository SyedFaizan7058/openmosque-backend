package com.openmosque.modules.moderation.repository;

import com.openmosque.modules.moderation.entity.MosqueSubmission;
import com.openmosque.modules.moderation.entity.SubmissionStatus;
import com.openmosque.modules.moderation.entity.SubmissionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MosqueSubmissionRepository extends JpaRepository<MosqueSubmission, UUID> {

    Page<MosqueSubmission> findByStatusAndDeletedFalse(SubmissionStatus status, Pageable pageable);

    Page<MosqueSubmission> findBySubmitterIdAndDeletedFalse(UUID submitterId, Pageable pageable);

    Page<MosqueSubmission> findBySubmissionTypeAndStatusAndDeletedFalse(
            SubmissionType submissionType, SubmissionStatus status, Pageable pageable);

    long countByStatus(SubmissionStatus status);
}
