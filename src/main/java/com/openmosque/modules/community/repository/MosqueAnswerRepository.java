package com.openmosque.modules.community.repository;

import com.openmosque.modules.community.entity.ContentStatus;
import com.openmosque.modules.community.entity.MosqueAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MosqueAnswerRepository extends JpaRepository<MosqueAnswer, UUID> {

    List<MosqueAnswer> findByQuestionIdAndStatusAndDeletedFalseOrderByOfficialMosqueAdminDescCreatedAtAsc(UUID questionId, ContentStatus status);
}
