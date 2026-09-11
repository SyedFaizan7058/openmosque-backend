package com.openmosque;

import com.openmosque.common.exception.ForbiddenException;
import com.openmosque.common.util.GeoUtils;
import com.openmosque.modules.claim.dto.MosqueClaimDecisionDto;
import com.openmosque.modules.claim.entity.ClaimStatus;
import com.openmosque.modules.claim.entity.MosqueClaimRequest;
import com.openmosque.modules.claim.repository.MosqueClaimRequestRepository;
import com.openmosque.modules.claim.service.MosqueClaimService;
import com.openmosque.modules.moderation.dto.SubmissionDecisionDto;
import com.openmosque.modules.moderation.entity.ModerationLog;
import com.openmosque.modules.moderation.entity.MosqueSubmission;
import com.openmosque.modules.moderation.entity.SubmissionStatus;
import com.openmosque.modules.moderation.entity.SubmissionType;
import com.openmosque.modules.moderation.repository.ModerationLogRepository;
import com.openmosque.modules.moderation.repository.MosqueSubmissionRepository;
import com.openmosque.modules.moderation.service.ModerationService;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class SecurityHardeningTests {

    @Autowired
    private ModerationService moderationService;

    @Autowired
    private MosqueSubmissionRepository submissionRepository;

    @Autowired
    private ModerationLogRepository moderationLogRepository;

    @Autowired
    private MosqueClaimService claimService;

    @Autowired
    private MosqueClaimRequestRepository claimRepository;

    @Autowired
    private MosqueRepository mosqueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private com.openmosque.modules.prayer.repository.MosquePrayerConfigRepository prayerConfigRepository;

    @Autowired(required = false)
    private com.openmosque.modules.prayer.repository.MosqueIqamahScheduleRepository iqamahScheduleRepository;

    @Autowired(required = false)
    private com.openmosque.modules.event.repository.MosqueEventRepository eventRepository;

    @Autowired(required = false)
    private com.openmosque.modules.event.repository.MosqueKhutbahRepository khutbahRepository;

    @Autowired(required = false)
    private com.openmosque.modules.community.repository.MosqueReviewRepository reviewRepository;

    @Autowired(required = false)
    private com.openmosque.modules.community.repository.MosqueAnswerRepository answerRepository;

    @Autowired(required = false)
    private com.openmosque.modules.community.repository.MosqueQuestionRepository questionRepository;

    @Autowired(required = false)
    private com.openmosque.modules.community.repository.CommunityContentFlagRepository flagRepository;

    @Autowired(required = false)
    private com.openmosque.modules.mosque.repository.UserFavoriteMosqueRepository favoriteRepository;

    private User moderator;
    private User superAdmin;
    private Mosque testMosque;

    @BeforeEach
    void setUp() {
        if (favoriteRepository != null) favoriteRepository.deleteAll();
        if (flagRepository != null) flagRepository.deleteAll();
        if (answerRepository != null) answerRepository.deleteAll();
        if (questionRepository != null) questionRepository.deleteAll();
        if (reviewRepository != null) reviewRepository.deleteAll();
        if (eventRepository != null) eventRepository.deleteAll();
        if (khutbahRepository != null) khutbahRepository.deleteAll();
        if (iqamahScheduleRepository != null) iqamahScheduleRepository.deleteAll();
        if (prayerConfigRepository != null) prayerConfigRepository.deleteAll();
        moderationLogRepository.deleteAll();
        claimRepository.deleteAll();
        submissionRepository.deleteAll();
        mosqueRepository.deleteAll();

        String salt = UUID.randomUUID().toString().substring(0, 8);
        moderator = userRepository.save(User.builder()
                .firebaseUid("mod-" + salt)
                .email("mod-" + salt + "@openmosque.org")
                .displayName("Moderator User")
                .role(UserRole.MODERATOR)
                .active(true)
                .build());

        superAdmin = userRepository.save(User.builder()
                .firebaseUid("admin-" + salt)
                .email("admin-" + salt + "@openmosque.org")
                .displayName("Super Admin")
                .role(UserRole.SUPER_ADMIN)
                .active(true)
                .build());

        testMosque = mosqueRepository.save(Mosque.builder()
                .name("Audit Mosque " + salt)
                .slug("audit-mosque-" + salt)
                .address("100 Safety Lane")
                .city("Birmingham")
                .country("United Kingdom")
                .latitude(52.4862)
                .longitude(-1.8904)
                .location(GeoUtils.createPoint(52.4862, -1.8904))
                .verified(true)
                .build());
    }

    @Test
    @DisplayName("Self-approval prevention: Moderator cannot approve their own mosque submission")
    void testModeratorCannotSelfApproveSubmission() {
        MosqueSubmission submission = submissionRepository.save(MosqueSubmission.builder()
                .name("Moderator Submission")
                .address("123 Main Road")
                .city("Birmingham")
                .country("UK")
                .latitude(52.4862)
                .longitude(-1.8904)
                .submitter(moderator)
                .submissionType(SubmissionType.NEW_MOSQUE)
                .status(SubmissionStatus.PENDING)
                .build());

        SubmissionDecisionDto decision = SubmissionDecisionDto.builder()
                .status(SubmissionStatus.APPROVED)
                .reviewComments("Attempting self approval")
                .build();

        assertThatThrownBy(() -> moderationService.reviewSubmission(submission.getId(), decision, moderator))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Conflict of interest: Moderators cannot review or approve their own submissions.");
    }

    @Test
    @DisplayName("Self-approval prevention: Admin cannot approve their own mosque claim request")
    void testAdminCannotSelfApproveClaim() {
        MosqueClaimRequest claim = claimRepository.save(MosqueClaimRequest.builder()
                .mosque(testMosque)
                .claimant(superAdmin)
                .fullName("Super Admin Claimant")
                .phoneNumber("+44123456789")
                .positionInMosque("Trustee")
                .status(ClaimStatus.PENDING)
                .build());

        MosqueClaimDecisionDto decision = MosqueClaimDecisionDto.builder()
                .status(ClaimStatus.APPROVED)
                .reviewComments("Self-approving claim")
                .build();

        assertThatThrownBy(() -> claimService.reviewClaim(claim.getId(), decision, superAdmin))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Conflict of interest: Claimants cannot review or approve their own claim requests.");
    }

    @Test
    @DisplayName("Distinct reviewer can successfully review submission and claim")
    void testDistinctReviewerCanApprove() {
        MosqueSubmission submission = submissionRepository.save(MosqueSubmission.builder()
                .name("Peer Submission Mosque")
                .address("456 High Street")
                .city("Birmingham")
                .country("UK")
                .latitude(52.4862)
                .longitude(-1.8904)
                .submitter(moderator)
                .submissionType(SubmissionType.NEW_MOSQUE)
                .status(SubmissionStatus.PENDING)
                .build());

        SubmissionDecisionDto decision = SubmissionDecisionDto.builder()
                .status(SubmissionStatus.APPROVED)
                .reviewComments("Approved by peer")
                .build();

        var response = moderationService.reviewSubmission(submission.getId(), decision, superAdmin);
        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.APPROVED);
    }
}
