package com.openmosque.modules.community.repository;

import com.openmosque.modules.community.entity.MosqueQuestion;
import com.openmosque.modules.community.entity.QuestionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MosqueQuestionRepository extends JpaRepository<MosqueQuestion, UUID> {

    @Query("SELECT q FROM MosqueQuestion q WHERE q.mosque.id = :mosqueId AND q.deleted = false AND q.status != 'HIDDEN' ORDER BY q.createdAt DESC")
    Page<MosqueQuestion> findPublicQuestionsByMosqueId(@Param("mosqueId") UUID mosqueId, Pageable pageable);

    List<MosqueQuestion> findByMosqueIdAndDeletedFalseOrderByCreatedAtDesc(UUID mosqueId);

    long countByMosqueIdAndStatusAndDeletedFalse(UUID mosqueId, QuestionStatus status);
}
