package com.openmosque.modules.community.service;

import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.modules.community.dto.AnswerCreateDto;
import com.openmosque.modules.community.dto.AnswerResponseDto;
import com.openmosque.modules.community.dto.QuestionCreateDto;
import com.openmosque.modules.community.dto.QuestionResponseDto;
import com.openmosque.modules.community.entity.ContentStatus;
import com.openmosque.modules.community.entity.MosqueAnswer;
import com.openmosque.modules.community.entity.MosqueQuestion;
import com.openmosque.modules.community.entity.QuestionStatus;
import com.openmosque.modules.community.mapper.CommunityMapper;
import com.openmosque.modules.community.repository.MosqueAnswerRepository;
import com.openmosque.modules.community.repository.MosqueQuestionRepository;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.service.MosqueService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service managing community Q&A interactions and official Imam answers.
 *
 * WHY THIS IS WRITTEN:
 * Enables worshippers to inquire about mosque policies, parking, wheelchair access, and women's facilities.
 * Distinguishes official answers from mosque administration (Imams/Trustees) via an official badge.
 *
 * WHERE IT IS USED:
 * - CommunityPublicController: Public Q&A thread retrieval (/api/v1/mosques/{idOrSlug}/questions)
 * - CommunityUserController: Authenticated question asking & answering
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityQAService {

    private final MosqueQuestionRepository questionRepository;
    private final MosqueAnswerRepository answerRepository;
    private final MosqueService mosqueService;
    private final CommunityMapper communityMapper;
    private final ProfanityFilterService profanityFilterService;
    private final com.openmosque.modules.claim.repository.MosqueClaimRequestRepository claimRepository;
    private final com.openmosque.modules.notification.service.NotificationService notificationService;

    /**
     * Retrieves paginated public questions and their associated published answers.
     * Answers posted by official mosque administrators are sorted first.
     *
     * @param idOrSlug Mosque UUID or unique slug
     * @param pageable Pagination parameters
     * @return Paginated list of question threads
     */
    @Transactional(readOnly = true)
    public Page<QuestionResponseDto> getQuestions(String idOrSlug, Pageable pageable) {
        Mosque mosque = mosqueService.findEntityByIdOrSlug(idOrSlug);
        Page<MosqueQuestion> page = questionRepository.findPublicQuestionsByMosqueId(mosque.getId(), pageable);

        return page.map(q -> {
            QuestionResponseDto dto = communityMapper.toDto(q);
            List<MosqueAnswer> answers = answerRepository.findByQuestionIdAndStatusAndDeletedFalseOrderByOfficialMosqueAdminDescCreatedAtAsc(
                    q.getId(), ContentStatus.PUBLISHED);
            dto.setAnswers(communityMapper.toAnswerDtoList(answers));
            return dto;
        });
    }

    /**
     * Posts a new community question for a mosque.
     * Enforces profanity and spam checks.
     *
     * @param mosqueId Target mosque UUID
     * @param dto Question payload
     * @param user Authenticated user asking the question
     * @return Created question details
     */
    @Transactional
    public QuestionResponseDto createQuestion(UUID mosqueId, QuestionCreateDto dto, User user) {
        profanityFilterService.validateCleanContent(dto.getQuestionText(), "questionText");
        Mosque mosque = mosqueService.findEntityByIdOrSlug(mosqueId.toString());

        MosqueQuestion question = MosqueQuestion.builder()
                .mosque(mosque)
                .user(user)
                .questionText(dto.getQuestionText())
                .status(QuestionStatus.OPEN)
                .build();

        MosqueQuestion saved = questionRepository.save(question);
        log.info("New community question asked on mosque '{}' by user '{}'", mosque.getName(), user.getEmail());
        return communityMapper.toDto(saved);
    }

    /**
     * Answers a community question.
     * Automatically assigns isOfficialMosqueAdmin = true if answered by a MOSQUE_ADMIN or SUPER_ADMIN.
     *
     * @param questionId Target question UUID
     * @param dto Answer payload
     * @param user Authenticated user writing the answer
     * @return Created answer details
     */
    @Transactional
    public AnswerResponseDto createAnswer(UUID questionId, AnswerCreateDto dto, User user) {
        profanityFilterService.validateCleanContent(dto.getAnswerText(), "answerText");

        MosqueQuestion question = questionRepository.findById(questionId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueQuestion", "id", questionId));

        boolean isOfficialAdmin = (user.getRole() == UserRole.MOSQUE_ADMIN || user.getRole() == UserRole.SUPER_ADMIN);

        MosqueAnswer answer = MosqueAnswer.builder()
                .question(question)
                .user(user)
                .answerText(dto.getAnswerText())
                .officialMosqueAdmin(isOfficialAdmin)
                .status(ContentStatus.PUBLISHED)
                .build();

        MosqueAnswer saved = answerRepository.save(answer);

        if (question.getStatus() == QuestionStatus.OPEN) {
            question.setStatus(QuestionStatus.ANSWERED);
            questionRepository.save(question);
        }

        if (question.getUser() != null && !question.getUser().getId().equals(user.getId())) {
            String responderName = user.getDisplayName() != null ? user.getDisplayName() : "A community member";
            notificationService.notifyUser(
                    question.getUser(),
                    "New Answer to Your Question",
                    (isOfficialAdmin ? "Mosque Admin (" : "(") + responderName + ") replied to your question on " + question.getMosque().getName(),
                    com.openmosque.modules.notification.entity.NotificationType.QUESTION_ANSWERED,
                    "/mosques/" + question.getMosque().getSlug(),
                    null
            );
        }

        log.info("Answer posted for question '{}' by user '{}' (official: {})", questionId, user.getEmail(), isOfficialAdmin);
        return communityMapper.toDto(saved);
    }

    /**
     * Updates an existing question. Only author or SUPER_ADMIN permitted.
     */
    @Transactional
    public QuestionResponseDto updateQuestion(UUID questionId, QuestionCreateDto dto, User user) {
        profanityFilterService.validateCleanContent(dto.getQuestionText(), "questionText");

        MosqueQuestion question = questionRepository.findById(questionId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueQuestion", "id", questionId));

        com.openmosque.common.util.SecurityUtils.requireOwnerOrSuperAdmin(user, question.getUser().getId(), "question");

        question.setQuestionText(dto.getQuestionText());
        MosqueQuestion updated = questionRepository.save(question);
        log.info("Updated question '{}' by user '{}'", questionId, user.getEmail());
        return communityMapper.toDto(updated);
    }

    /**
     * Deletes a question. Author, Moderator, Super Admin, or Mosque Admin for this mosque permitted.
     */
    @Transactional
    public void deleteQuestion(UUID questionId, User user) {
        MosqueQuestion question = questionRepository.findById(questionId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueQuestion", "id", questionId));

        boolean isMosqueAdminForThisMosque = false;
        if (user.getRole() == UserRole.MOSQUE_ADMIN) {
            isMosqueAdminForThisMosque = claimRepository.findByMosqueIdAndClaimantIdAndStatus(
                    question.getMosque().getId(), user.getId(), com.openmosque.modules.claim.entity.ClaimStatus.APPROVED).isPresent();
        }

        com.openmosque.common.util.SecurityUtils.requireDeletePermission(user, question.getUser().getId(), isMosqueAdminForThisMosque, "question");

        question.setDeleted(true);
        questionRepository.save(question);
        log.info("Soft-deleted question '{}' by user '{}'", questionId, user.getEmail());
    }

    /**
     * Updates an existing answer. Only author or SUPER_ADMIN permitted.
     */
    @Transactional
    public AnswerResponseDto updateAnswer(UUID answerId, AnswerCreateDto dto, User user) {
        profanityFilterService.validateCleanContent(dto.getAnswerText(), "answerText");

        MosqueAnswer answer = answerRepository.findById(answerId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueAnswer", "id", answerId));

        com.openmosque.common.util.SecurityUtils.requireOwnerOrSuperAdmin(user, answer.getUser().getId(), "answer");

        answer.setAnswerText(dto.getAnswerText());
        MosqueAnswer updated = answerRepository.save(answer);
        log.info("Updated answer '{}' by user '{}'", answerId, user.getEmail());
        return communityMapper.toDto(updated);
    }

    /**
     * Deletes an answer. Author, Moderator, Super Admin, or Mosque Admin for this mosque permitted.
     */
    @Transactional
    public void deleteAnswer(UUID answerId, User user) {
        MosqueAnswer answer = answerRepository.findById(answerId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueAnswer", "id", answerId));

        boolean isMosqueAdminForThisMosque = false;
        if (user.getRole() == UserRole.MOSQUE_ADMIN) {
            isMosqueAdminForThisMosque = claimRepository.findByMosqueIdAndClaimantIdAndStatus(
                    answer.getQuestion().getMosque().getId(), user.getId(), com.openmosque.modules.claim.entity.ClaimStatus.APPROVED).isPresent();
        }

        com.openmosque.common.util.SecurityUtils.requireDeletePermission(user, answer.getUser().getId(), isMosqueAdminForThisMosque, "answer");

        answer.setDeleted(true);
        answerRepository.save(answer);
        log.info("Soft-deleted answer '{}' by user '{}'", answerId, user.getEmail());
    }
}
