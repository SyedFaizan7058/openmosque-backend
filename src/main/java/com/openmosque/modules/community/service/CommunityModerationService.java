package com.openmosque.modules.community.service;

import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.common.util.SecurityUtils;
import com.openmosque.modules.community.dto.ContentFlagCreateDto;
import com.openmosque.modules.community.dto.ContentFlagResponseDto;
import com.openmosque.modules.community.dto.FlagDecisionDto;
import com.openmosque.modules.community.entity.*;
import com.openmosque.modules.community.mapper.CommunityMapper;
import com.openmosque.modules.community.repository.CommunityContentFlagRepository;
import com.openmosque.modules.community.repository.MosqueAnswerRepository;
import com.openmosque.modules.community.repository.MosqueQuestionRepository;
import com.openmosque.modules.community.repository.MosqueReviewRepository;
import com.openmosque.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service managing community content reporting and moderator decision workflows.
 *
 * WHY THIS IS WRITTEN:
 * Protects platform integrity by allowing worshippers to flag spam, abuse, or offensive content.
 * Provides community moderators with an organized review queue to audit reported reviews, questions,
 * and answers, cascading automatic content hiding upon confirmed violation.
 *
 * WHERE IT IS USED:
 * - CommunityUserController: Authenticated reporting endpoint (/api/v1/community/flag)
 * - CommunityAdminModerationController: Moderator dashboard (/api/v1/admin/community/flags)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityModerationService {

    private final CommunityContentFlagRepository flagRepository;
    private final MosqueReviewRepository reviewRepository;
    private final MosqueQuestionRepository questionRepository;
    private final MosqueAnswerRepository answerRepository;
    private final CommunityMapper communityMapper;

    /**
     * Submits a report/flag against an inappropriate review, question, or answer.
     *
     * @param dto Report details (target type, target UUID, violation reason)
     * @param reporter Authenticated user reporting the content
     * @return Created flag DTO with PENDING status
     */
    @Transactional
    public ContentFlagResponseDto flagContent(ContentFlagCreateDto dto, User reporter) {
        CommunityContentFlag flag = CommunityContentFlag.builder()
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .reporter(reporter)
                .reason(dto.getReason())
                .status(FlagStatus.PENDING)
                .build();

        CommunityContentFlag saved = flagRepository.save(flag);
        log.warn("Community content flagged: Type '{}', ID '{}' by reporter '{}'",
                dto.getTargetType(), dto.getTargetId(), reporter.getEmail());
        return communityMapper.toDto(saved);
    }

    /**
     * Retrieves queue of all pending content flags requiring review.
     *
     * @param pageable Pagination parameters
     * @return Paginated list of pending flags
     */
    @Transactional(readOnly = true)
    public Page<ContentFlagResponseDto> getPendingFlags(Pageable pageable) {
        return flagRepository.findByStatusAndDeletedFalseOrderByCreatedAtDesc(FlagStatus.PENDING, pageable)
                .map(communityMapper::toDto);
    }

    /**
     * Evaluates and decides on a content flag.
     * Enforces MODERATOR or SUPER_ADMIN authorization.
     * If decision is RESOLVED, automatically hides the target content from public listings.
     *
     * @param flagId Target flag UUID
     * @param decision Decision payload (RESOLVED or DISMISSED + reviewer notes)
     * @param reviewer Authenticated moderator or super admin
     * @return Updated flag DTO
     */
    @Transactional
    public ContentFlagResponseDto decideFlag(UUID flagId, FlagDecisionDto decision, User reviewer) {
        SecurityUtils.requireModeratorOrSuperAdmin(reviewer, "review and decide on content flags");

        CommunityContentFlag flag = flagRepository.findById(flagId)
                .filter(f -> !f.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("CommunityContentFlag", "id", flagId));

        flag.setStatus(decision.getStatus());
        flag.setReviewer(reviewer);
        flag.setReviewerNotes(decision.getReviewerNotes());

        // If violation is confirmed (RESOLVED), hide the target content
        if (decision.getStatus() == FlagStatus.RESOLVED) {
            hideTargetContent(flag.getTargetType(), flag.getTargetId());
        }

        CommunityContentFlag updated = flagRepository.save(flag);
        log.info("Flag '{}' decided as '{}' by moderator '{}'", flagId, decision.getStatus(), reviewer.getEmail());
        return communityMapper.toDto(updated);
    }

    private void hideTargetContent(TargetType targetType, UUID targetId) {
        switch (targetType) {
            case REVIEW -> reviewRepository.findById(targetId).ifPresent(r -> {
                r.setStatus(ContentStatus.HIDDEN);
                reviewRepository.save(r);
                log.info("Review '{}' hidden by moderation action", targetId);
            });
            case QUESTION -> questionRepository.findById(targetId).ifPresent(q -> {
                q.setStatus(QuestionStatus.HIDDEN);
                questionRepository.save(q);
                log.info("Question '{}' hidden by moderation action", targetId);
            });
            case ANSWER -> answerRepository.findById(targetId).ifPresent(a -> {
                a.setStatus(ContentStatus.HIDDEN);
                answerRepository.save(a);
                log.info("Answer '{}' hidden by moderation action", targetId);
            });
        }
    }
}
