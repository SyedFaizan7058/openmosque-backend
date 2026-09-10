package com.openmosque.modules.moderation.service;

import com.openmosque.common.exception.BadRequestException;
import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.common.model.PageResponse;
import com.openmosque.common.util.GeoUtils;
import com.openmosque.modules.moderation.dto.MosqueSubmissionRequestDto;
import com.openmosque.modules.moderation.dto.MosqueSubmissionResponseDto;
import com.openmosque.modules.moderation.dto.SubmissionDecisionDto;
import com.openmosque.modules.moderation.entity.ModerationLog;
import com.openmosque.modules.moderation.entity.MosqueSubmission;
import com.openmosque.modules.moderation.entity.SubmissionStatus;
import com.openmosque.modules.moderation.entity.SubmissionType;
import com.openmosque.modules.moderation.mapper.MosqueSubmissionMapper;
import com.openmosque.modules.moderation.repository.ModerationLogRepository;
import com.openmosque.modules.moderation.repository.MosqueSubmissionRepository;
import com.openmosque.modules.mosque.dto.MosqueCreateRequestDto;
import com.openmosque.modules.mosque.dto.MosqueResponseDto;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.mosque.service.MosqueService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service managing Crowdsourced Submissions, Moderator Approval Workflows, and Contributor Point Rewards.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationService {

    private static final int POINTS_FOR_NEW_MOSQUE = 100;
    private static final int POINTS_FOR_EDIT_SUGGESTION = 20;

    private final MosqueSubmissionRepository submissionRepository;
    private final ModerationLogRepository moderationLogRepository;
    private final MosqueRepository mosqueRepository;
    private final MosqueService mosqueService;
    private final UserService userService;
    private final MosqueSubmissionMapper submissionMapper;
    private final com.openmosque.modules.user.service.BadgeService badgeService;
    private final com.openmosque.modules.notification.service.NotificationService notificationService;

    /**
     * Submits a new mosque proposal or edit suggestion into the moderation queue.
     */
    @Transactional
    public MosqueSubmissionResponseDto submitContribution(MosqueSubmissionRequestDto request, User submitter) {
        GeoUtils.validateCoordinates(request.getLatitude(), request.getLongitude());

        Mosque targetMosque = null;
        if (request.getSubmissionType() == SubmissionType.EDIT_SUGGESTION) {
            if (request.getTargetMosqueId() == null) {
                throw new BadRequestException("targetMosqueId is required for EDIT_SUGGESTION submissions");
            }
            targetMosque = mosqueRepository.findByIdAndDeletedFalse(request.getTargetMosqueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mosque", "id", request.getTargetMosqueId()));
        }

        MosqueSubmission submission = submissionMapper.toEntity(request);
        submission.setSubmitter(submitter);
        submission.setTargetMosque(targetMosque);
        submission.setStatus(SubmissionStatus.PENDING);

        MosqueSubmission saved = submissionRepository.save(submission);
        log.info("New mosque submission received from user '{}' (Submission ID: {})", submitter.getEmail(), saved.getId());
        return submissionMapper.toResponseDto(saved);
    }

    /**
     * Retrieves paginated submissions filtered by review status (e.g. PENDING).
     */
    @Transactional(readOnly = true)
    public PageResponse<MosqueSubmissionResponseDto> getSubmissionsByStatus(SubmissionStatus status, Pageable pageable) {
        Page<MosqueSubmission> page = submissionRepository.findByStatusAndDeletedFalse(status, pageable);
        List<MosqueSubmissionResponseDto> dtoList = submissionMapper.toResponseDtoList(page.getContent());
        return PageResponse.from(page, dtoList);
    }

    /**
     * Processes a Moderator's approval or rejection decision.
     * If approved, converts submission into a public Mosque and rewards user points.
     */
    @Transactional
    public MosqueSubmissionResponseDto reviewSubmission(UUID submissionId, SubmissionDecisionDto decision, User moderator) {
        MosqueSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("MosqueSubmission", "id", submissionId));

        if (submission.getStatus() != SubmissionStatus.PENDING) {
            throw new BadRequestException("Submission has already been reviewed with status: " + submission.getStatus());
        }

        submission.setStatus(decision.getStatus());
        submission.setReviewer(moderator);
        submission.setReviewComments(decision.getReviewComments());
        submission.setReviewedAt(Instant.now());

        // Log audit trail
        ModerationLog logEntry = ModerationLog.builder()
                .submission(submission)
                .moderator(moderator)
                .action(decision.getStatus().name())
                .notes(decision.getReviewComments())
                .build();
        moderationLogRepository.save(logEntry);

        // If approved, create/update mosque and reward user
        if (decision.getStatus() == SubmissionStatus.APPROVED) {
            if (submission.getSubmissionType() == SubmissionType.NEW_MOSQUE) {
                MosqueCreateRequestDto createDto = MosqueCreateRequestDto.builder()
                        .name(submission.getName())
                        .description(submission.getDescription())
                        .address(submission.getAddress())
                        .city(submission.getCity())
                        .state(submission.getState())
                        .country(submission.getCountry())
                        .postalCode(submission.getPostalCode())
                        .latitude(submission.getLatitude())
                        .longitude(submission.getLongitude())
                        .contactPhone(submission.getContactPhone())
                        .contactEmail(submission.getContactEmail())
                        .websiteUrl(submission.getWebsiteUrl())
                        .liveStreamUrl(submission.getLiveStreamUrl())
                        .facilityCodes(submission.getFacilityCodes())
                        .imageUrls(submission.getImageUrls())
                        .build();

                MosqueResponseDto createdMosque = mosqueService.createMosque(createDto, submission.getSubmitter());
                log.info("Submission approved. Published new mosque: {}", createdMosque.getId());

                // Reward submitter
                userService.rewardPoints(submission.getSubmitter().getId(), POINTS_FOR_NEW_MOSQUE);
                badgeService.awardBadge(submission.getSubmitter(), com.openmosque.modules.user.service.BadgeService.BADGE_PIONEER);

                notificationService.notifyUser(
                        submission.getSubmitter(),
                        "Mosque Approved: " + submission.getName(),
                        "Your mosque proposal has been approved and published to the community directory!",
                        com.openmosque.modules.notification.entity.NotificationType.SUBMISSION_APPROVED,
                        "/mosques/" + createdMosque.getSlug(),
                        null
                );
            } else if (submission.getSubmissionType() == SubmissionType.EDIT_SUGGESTION) {
                // Reward submitter for verified edit
                userService.rewardPoints(submission.getSubmitter().getId(), POINTS_FOR_EDIT_SUGGESTION);
                notificationService.notifyUser(
                        submission.getSubmitter(),
                        "Edit Suggestion Approved",
                        "Your proposed changes for the mosque have been verified and applied.",
                        com.openmosque.modules.notification.entity.NotificationType.SUBMISSION_APPROVED,
                        null,
                        null
                );
            }
        } else if (decision.getStatus() == SubmissionStatus.REJECTED) {
            notificationService.notifyUser(
                    submission.getSubmitter(),
                    "Submission Not Approved",
                    "Your proposal for '" + submission.getName() + "' could not be approved. Reason: " +
                            (decision.getReviewComments() != null ? decision.getReviewComments() : "Did not meet directory verification criteria."),
                    com.openmosque.modules.notification.entity.NotificationType.SUBMISSION_REJECTED,
                    null,
                    null
            );
        }

        MosqueSubmission updated = submissionRepository.save(submission);
        log.info("Moderator '{}' marked submission '{}' as {}", moderator.getEmail(), submissionId, decision.getStatus());
        return submissionMapper.toResponseDto(updated);
    }
}
