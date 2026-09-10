package com.openmosque;

import com.openmosque.common.util.GeoUtils;
import com.openmosque.modules.moderation.dto.MosqueSubmissionRequestDto;
import com.openmosque.modules.moderation.dto.MosqueSubmissionResponseDto;
import com.openmosque.modules.moderation.dto.SubmissionDecisionDto;
import com.openmosque.modules.moderation.entity.SubmissionStatus;
import com.openmosque.modules.moderation.entity.SubmissionType;
import com.openmosque.modules.moderation.service.ModerationService;
import com.openmosque.modules.mosque.dto.FacilityDto;
import com.openmosque.modules.mosque.dto.MosqueCreateRequestDto;
import com.openmosque.modules.mosque.dto.MosqueResponseDto;
import com.openmosque.modules.mosque.dto.MosqueSummaryDto;
import com.openmosque.modules.mosque.entity.Facility;
import com.openmosque.modules.mosque.repository.FacilityRepository;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.mosque.service.FacilityService;
import com.openmosque.modules.mosque.service.MosqueService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MosqueServiceTests {

    @Autowired
    private MosqueService mosqueService;

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private ModerationService moderationService;

    @Autowired
    private MosqueRepository mosqueRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.openmosque.modules.moderation.repository.ModerationLogRepository moderationLogRepository;

    @Autowired
    private com.openmosque.modules.moderation.repository.MosqueSubmissionRepository submissionRepository;

    @Autowired
    private com.openmosque.modules.claim.repository.MosqueClaimRequestRepository claimRepository;

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

    private User testUser;
    private User testModerator;
    private Facility wuduFacility;
    private Facility womensFacility;
    private Facility parkingFacility;

    @BeforeEach
    void setUp() {
        if (favoriteRepository != null) favoriteRepository.deleteAll();
        if (flagRepository != null) flagRepository.deleteAll();
        if (answerRepository != null) answerRepository.deleteAll();
        if (questionRepository != null) questionRepository.deleteAll();
        if (reviewRepository != null) reviewRepository.deleteAll();
        if (eventRepository != null) eventRepository.deleteAll();
        if (khutbahRepository != null) khutbahRepository.deleteAll();
        if (prayerConfigRepository != null) prayerConfigRepository.deleteAll();
        if (iqamahScheduleRepository != null) iqamahScheduleRepository.deleteAll();
        moderationLogRepository.deleteAll();
        submissionRepository.deleteAll();
        claimRepository.deleteAll();
        mosqueRepository.deleteAll();
        facilityRepository.deleteAll();

        // Seed test facilities
        wuduFacility = facilityRepository.save(Facility.builder()
                .code("WUDU_AREA")
                .name("Wudu Area")
                .description("Ablution facilities")
                .active(true)
                .build());

        womensFacility = facilityRepository.save(Facility.builder()
                .code("WOMENS_SECTION")
                .name("Women's Section")
                .description("Dedicated space for women")
                .active(true)
                .build());

        parkingFacility = facilityRepository.save(Facility.builder()
                .code("PARKING")
                .name("Parking")
                .description("Car parking available")
                .active(true)
                .build());

        // Create test submitter and moderator
        testUser = userRepository.save(User.builder()
                .firebaseUid("submitter-" + UUID.randomUUID())
                .email("contributor-" + UUID.randomUUID() + "@openmosque.org")
                .displayName("Brother Tariq")
                .role(UserRole.USER)
                .points(0)
                .active(true)
                .build());

        testModerator = userRepository.save(User.builder()
                .firebaseUid("mod-" + UUID.randomUUID())
                .email("moderator-" + UUID.randomUUID() + "@openmosque.org")
                .displayName("Moderator Farooq")
                .role(UserRole.MODERATOR)
                .points(0)
                .active(true)
                .build());
    }

    @Test
    @DisplayName("FacilityService retrieves active amenities")
    void testGetFacilities() {
        List<FacilityDto> facilities = facilityService.getAllActiveFacilities();
        assertThat(facilities).hasSizeGreaterThanOrEqualTo(3);
        assertThat(facilities).extracting("code").contains("WUDU_AREA", "WOMENS_SECTION", "PARKING");
    }

    @Test
    @DisplayName("Mosque creation, slug generation, and profile lookup works")
    void testCreateAndGetMosque() {
        MosqueCreateRequestDto request = MosqueCreateRequestDto.builder()
                .name("East London Mosque")
                .description("One of the largest mosques in Europe")
                .address("82-92 Whitechapel Rd")
                .city("London")
                .country("United Kingdom")
                .latitude(51.5173)
                .longitude(-0.0658)
                .facilityCodes(List.of("WUDU_AREA", "WOMENS_SECTION", "PARKING"))
                .imageUrls(List.of("https://example.com/elm-cover.jpg", "https://example.com/elm-interior.jpg"))
                .build();

        MosqueResponseDto response = mosqueService.createMosque(request, testUser);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getSlug()).startsWith("east-london-mosque-london");
        assertThat(response.getFacilities()).hasSize(3);
        assertThat(response.getImages()).hasSize(2);

        // Fetch by slug
        MosqueResponseDto fetchedBySlug = mosqueService.getMosqueByIdOrSlug(response.getSlug());
        assertThat(fetchedBySlug.getName()).isEqualTo("East London Mosque");
    }

    @Test
    @DisplayName("Nearby radius search calculates distance and filters facilities accurately")
    void testNearbyMosquesWithFacilityFilter() {
        // Mosque A: 1 km away with WUDU + WOMENS
        mosqueService.createMosque(MosqueCreateRequestDto.builder()
                .name("Close Mosque")
                .address("10 Main St")
                .city("London")
                .country("UK")
                .latitude(51.5180)
                .longitude(-0.0650)
                .facilityCodes(List.of("WUDU_AREA", "WOMENS_SECTION"))
                .build(), testUser);

        // Mosque B: 3 km away with WUDU only
        mosqueService.createMosque(MosqueCreateRequestDto.builder()
                .name("Further Mosque")
                .address("50 High St")
                .city("London")
                .country("UK")
                .latitude(51.5300)
                .longitude(-0.0500)
                .facilityCodes(List.of("WUDU_AREA"))
                .build(), testUser);

        // Mosque C: 50 km away (out of radius)
        mosqueService.createMosque(MosqueCreateRequestDto.builder()
                .name("Far Mosque")
                .address("100 Country Rd")
                .city("Oxford")
                .country("UK")
                .latitude(51.7520)
                .longitude(-1.2577)
                .facilityCodes(List.of("WUDU_AREA"))
                .build(), testUser);

        // Search near London (51.5173, -0.0658) with 10km radius
        List<MosqueSummaryDto> nearbyAll = mosqueService.getNearbyMosques(51.5173, -0.0658, 10.0, null);
        assertThat(nearbyAll).hasSize(2);
        assertThat(nearbyAll.get(0).getName()).isEqualTo("Close Mosque");
        assertThat(nearbyAll.get(0).getDistanceKm()).isLessThan(1.0);

        // Search with required facility 'WOMENS_SECTION'
        List<MosqueSummaryDto> nearbyWithWomen = mosqueService.getNearbyMosques(
                51.5173, -0.0658, 10.0, List.of("WOMENS_SECTION"));
        assertThat(nearbyWithWomen).hasSize(1);
        assertThat(nearbyWithWomen.get(0).getName()).isEqualTo("Close Mosque");
    }

    @Test
    @DisplayName("End-to-End Moderation: User submits mosque, Moderator approves, live entry created, points awarded")
    void testCrowdsourceAndModerationWorkflow() {
        int initialPoints = testUser.getPoints();

        // 1. User submits a new mosque
        MosqueSubmissionRequestDto submissionRequest = MosqueSubmissionRequestDto.builder()
                .submissionType(SubmissionType.NEW_MOSQUE)
                .name("Birmingham Central Mosque")
                .description("Historic mosque in Birmingham")
                .address("180 Belgrave Middleway")
                .city("Birmingham")
                .country("United Kingdom")
                .latitude(52.4678)
                .longitude(-1.8906)
                .facilityCodes(List.of("PARKING", "WUDU_AREA"))
                .imageUrls(List.of("https://example.com/birmingham-mosque.jpg"))
                .build();

        MosqueSubmissionResponseDto submission = moderationService.submitContribution(submissionRequest, testUser);
        assertThat(submission.getId()).isNotNull();
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.PENDING);

        // 2. Moderator approves the submission
        SubmissionDecisionDto decision = SubmissionDecisionDto.builder()
                .status(SubmissionStatus.APPROVED)
                .reviewComments("Verified address and facilities. Excellent contribution.")
                .build();

        MosqueSubmissionResponseDto approvedSubmission = moderationService.reviewSubmission(
                submission.getId(), decision, testModerator);

        assertThat(approvedSubmission.getStatus()).isEqualTo(SubmissionStatus.APPROVED);
        assertThat(approvedSubmission.getReviewerName()).isEqualTo("Moderator Farooq");

        // 3. Verify that the mosque is now public and searchable
        MosqueResponseDto publicMosque = mosqueService.getMosqueByIdOrSlug("birmingham-central-mosque-birmingham");
        assertThat(publicMosque).isNotNull();
        assertThat(publicMosque.getName()).isEqualTo("Birmingham Central Mosque");

        // 4. Verify submitter was rewarded with 100 points
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getPoints()).isEqualTo(initialPoints + 100);
    }
}
