package com.openmosque;

import com.openmosque.common.exception.ForbiddenException;
import com.openmosque.common.util.GeoUtils;
import com.openmosque.modules.mosque.dto.MosqueCreateRequestDto;
import com.openmosque.modules.mosque.dto.MosqueResponseDto;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.mosque.service.MosqueService;
import com.openmosque.modules.prayer.dto.*;
import com.openmosque.modules.prayer.entity.CalculationMethod;
import com.openmosque.modules.prayer.entity.IqamahCalculationType;
import com.openmosque.modules.prayer.entity.JuristicSchool;
import com.openmosque.modules.prayer.service.PrayerTimesService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PrayerTimesServiceTests {

    @Autowired
    private PrayerTimesService prayerTimesService;

    @Autowired
    private MosqueService mosqueService;

    @Autowired
    private MosqueRepository mosqueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.openmosque.modules.prayer.repository.MosquePrayerConfigRepository prayerConfigRepository;

    @Autowired
    private com.openmosque.modules.prayer.repository.MosqueIqamahScheduleRepository iqamahScheduleRepository;

    @Autowired
    private com.openmosque.modules.moderation.repository.MosqueSubmissionRepository submissionRepository;

    @Autowired
    private com.openmosque.modules.claim.repository.MosqueClaimRequestRepository claimRepository;

    @Autowired
    private com.openmosque.modules.moderation.repository.ModerationLogRepository moderationLogRepository;

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

    private User superAdmin;
    private User mosqueAdmin;
    private User regularUser;
    private Mosque testMosque;

    @BeforeEach
    void setUp() {
        if (flagRepository != null) flagRepository.deleteAll();
        if (answerRepository != null) answerRepository.deleteAll();
        if (questionRepository != null) questionRepository.deleteAll();
        if (reviewRepository != null) reviewRepository.deleteAll();
        if (eventRepository != null) eventRepository.deleteAll();
        if (khutbahRepository != null) khutbahRepository.deleteAll();
        prayerConfigRepository.deleteAll();
        iqamahScheduleRepository.deleteAll();
        moderationLogRepository.deleteAll();
        submissionRepository.deleteAll();
        claimRepository.deleteAll();
        mosqueRepository.deleteAll();

        String salt = UUID.randomUUID().toString().substring(0, 8);
        superAdmin = userRepository.save(User.builder()
                .firebaseUid("superadmin-" + salt)
                .email("admin-" + salt + "@openmosque.org")
                .displayName("Super Admin")
                .role(UserRole.SUPER_ADMIN)
                .active(true)
                .build());

        mosqueAdmin = userRepository.save(User.builder()
                .firebaseUid("imam-" + salt)
                .email("imam-" + salt + "@openmosque.org")
                .displayName("Head Imam")
                .role(UserRole.MOSQUE_ADMIN)
                .active(true)
                .build());

        regularUser = userRepository.save(User.builder()
                .firebaseUid("worshipper-" + salt)
                .email("user-" + salt + "@openmosque.org")
                .displayName("Regular User")
                .role(UserRole.USER)
                .active(true)
                .build());

        // Create active test mosque
        testMosque = Mosque.builder()
                .name("East London Central Mosque")
                .slug("east-london-central-mosque")
                .description("Historic mosque in London")
                .address("82 Whitechapel Road")
                .city("London")
                .country("United Kingdom")
                .latitude(51.5186)
                .longitude(-0.0655)
                .location(GeoUtils.createPoint(51.5186, -0.0655))
                .status(com.openmosque.modules.mosque.entity.MosqueStatus.ACTIVE)
                .verified(true)
                .build();
        testMosque = mosqueRepository.save(testMosque);

        claimRepository.save(com.openmosque.modules.claim.entity.MosqueClaimRequest.builder()
                .mosque(testMosque)
                .claimant(mosqueAdmin)
                .fullName("Head Imam")
                .phoneNumber("+44123456780")
                .positionInMosque("Imam")
                .status(com.openmosque.modules.claim.entity.ClaimStatus.APPROVED)
                .build());
    }

    @Test
    @DisplayName("Should fetch daily prayer times with auto-initialized default config and Iqamah offsets")
    void testGetPrayerTimesForMosque() {
        PrayerTimesDayResponseDto response = prayerTimesService.getPrayerTimesForMosque(
                testMosque.getId().toString(), LocalDate.now()
        );

        assertThat(response).isNotNull();
        assertThat(response.getMosqueName()).isEqualTo("East London Central Mosque");
        assertThat(response.getCalculationMethod()).isEqualTo("MUSLIM_WORLD_LEAGUE");
        assertThat(response.getJuristicSchool()).isEqualTo("STANDARD");
        assertThat(response.getTimings()).hasSize(6); // Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha
        assertThat(response.getJummahSchedule()).isNotNull();
        assertThat(response.getJummahSchedule().getFirstJummahTime()).isEqualTo("13:15");
    }

    @Test
    @DisplayName("Should update Mosque Prayer calculation method and juristic school")
    void testUpdatePrayerConfig() {
        PrayerConfigUpdateDto updateDto = PrayerConfigUpdateDto.builder()
                .calculationMethod(CalculationMethod.ISNA)
                .juristicSchool(JuristicSchool.HANAFI)
                .timeZone("Europe/London")
                .build();

        PrayerConfigDto updated = prayerTimesService.updatePrayerConfig(testMosque.getId(), updateDto, mosqueAdmin);

        assertThat(updated.getCalculationMethod()).isEqualTo(CalculationMethod.ISNA);
        assertThat(updated.getJuristicSchool()).isEqualTo(JuristicSchool.HANAFI);
        assertThat(updated.getTimeZone()).isEqualTo("Europe/London");
    }

    @Test
    @DisplayName("Should update Mosque Iqamah schedules and Friday Jumu'ah batches")
    void testUpdateIqamahSchedule() {
        IqamahScheduleUpdateDto updateDto = IqamahScheduleUpdateDto.builder()
                .fajrType(IqamahCalculationType.FIXED_TIME)
                .fajrFixedTime(LocalTime.of(5, 30))
                .dhuhrType(IqamahCalculationType.FIXED_TIME)
                .dhuhrFixedTime(LocalTime.of(13, 45))
                .asrType(IqamahCalculationType.OFFSET_AFTER_ADHAN)
                .asrOffsetMinutes(20)
                .maghribType(IqamahCalculationType.OFFSET_AFTER_ADHAN)
                .maghribOffsetMinutes(10)
                .ishaType(IqamahCalculationType.FIXED_TIME)
                .ishaFixedTime(LocalTime.of(21, 30))
                .jummah1Time(LocalTime.of(13, 0))
                .jummah2Time(LocalTime.of(14, 0))
                .jummahKhutbahLanguage("English & Arabic")
                .build();

        IqamahScheduleDto updated = prayerTimesService.updateIqamahSchedule(testMosque.getId(), updateDto, superAdmin);

        assertThat(updated.getFajrType()).isEqualTo(IqamahCalculationType.FIXED_TIME);
        assertThat(updated.getFajrFixedTime()).isEqualTo(LocalTime.of(5, 30));
        assertThat(updated.getDhuhrFixedTime()).isEqualTo(LocalTime.of(13, 45));
        assertThat(updated.getAsrOffsetMinutes()).isEqualTo(20);
        assertThat(updated.getJummah1Time()).isEqualTo(LocalTime.of(13, 0));
        assertThat(updated.getJummah2Time()).isEqualTo(LocalTime.of(14, 0));
        assertThat(updated.getJummahKhutbahLanguage()).isEqualTo("English & Arabic");
    }

    @Test
    @DisplayName("Should throw ForbiddenException when regular USER attempts to update prayer settings")
    void testForbiddenWhenRegularUserUpdatesConfig() {
        PrayerConfigUpdateDto updateDto = PrayerConfigUpdateDto.builder()
                .calculationMethod(CalculationMethod.KARACHI)
                .juristicSchool(JuristicSchool.STANDARD)
                .build();

        assertThatThrownBy(() -> prayerTimesService.updatePrayerConfig(testMosque.getId(), updateDto, regularUser))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You do not have permission to manage prayer timings for this mosque");
    }

    @Test
    @DisplayName("Should list all supported standard calculation methods")
    void testListCalculationMethods() {
        List<CalculationMethodDto> methods = prayerTimesService.getSupportedCalculationMethods();

        assertThat(methods).isNotEmpty();
        assertThat(methods).extracting("code")
                .contains("ISNA", "MUSLIM_WORLD_LEAGUE", "UMM_AL_QURA", "KARACHI", "EGYPTIAN");
    }
}
