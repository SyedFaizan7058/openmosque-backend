package com.openmosque;

import com.openmosque.common.exception.ForbiddenException;
import com.openmosque.common.util.GeoUtils;
import com.openmosque.modules.event.dto.MosqueEventCreateDto;
import com.openmosque.modules.event.dto.MosqueEventResponseDto;
import com.openmosque.modules.event.dto.MosqueKhutbahCreateDto;
import com.openmosque.modules.event.dto.MosqueKhutbahResponseDto;
import com.openmosque.modules.event.entity.EventAudience;
import com.openmosque.modules.event.entity.EventType;
import com.openmosque.modules.event.repository.MosqueEventRepository;
import com.openmosque.modules.event.repository.MosqueKhutbahRepository;
import com.openmosque.modules.event.service.MosqueEventService;
import com.openmosque.modules.event.service.MosqueKhutbahService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class MosqueEventServiceTests {

    @Autowired
    private MosqueEventService eventService;

    @Autowired
    private MosqueKhutbahService khutbahService;

    @Autowired
    private MosqueEventRepository eventRepository;

    @Autowired
    private MosqueKhutbahRepository khutbahRepository;

    @Autowired
    private MosqueRepository mosqueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private com.openmosque.modules.mosque.repository.UserFavoriteMosqueRepository favoriteRepository;

    private User mosqueAdmin;
    private User regularUser;
    private Mosque testMosque;

    @Autowired
    private com.openmosque.modules.claim.repository.MosqueClaimRequestRepository claimRequestRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        khutbahRepository.deleteAll();
        if (claimRequestRepository != null) claimRequestRepository.deleteAll();
        if (favoriteRepository != null) favoriteRepository.deleteAll();
        mosqueRepository.deleteAll();

        String salt = UUID.randomUUID().toString().substring(0, 8);
        mosqueAdmin = userRepository.save(User.builder()
                .firebaseUid("imam-" + salt)
                .email("imam-" + salt + "@openmosque.org")
                .displayName("Imam Hassan")
                .role(UserRole.MOSQUE_ADMIN)
                .active(true)
                .build());

        regularUser = userRepository.save(User.builder()
                .firebaseUid("worshipper-" + salt)
                .email("worshipper-" + salt + "@openmosque.org")
                .displayName("Brother Ali")
                .role(UserRole.USER)
                .active(true)
                .build());

        testMosque = mosqueRepository.save(Mosque.builder()
                .name("Cambridge Central Mosque")
                .slug("cambridge-central-mosque-" + salt)
                .address("Mill Road")
                .city("Cambridge")
                .country("United Kingdom")
                .latitude(52.1989)
                .longitude(0.1436)
                .location(GeoUtils.createPoint(52.1989, 0.1436))
                .verified(true)
                .build());

        claimRequestRepository.save(com.openmosque.modules.claim.entity.MosqueClaimRequest.builder()
                .mosque(testMosque)
                .claimant(mosqueAdmin)
                .fullName("Imam Hassan")
                .phoneNumber("+44123456789")
                .positionInMosque("Imam")
                .status(com.openmosque.modules.claim.entity.ClaimStatus.APPROVED)
                .build());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        eventRepository.deleteAll();
        khutbahRepository.deleteAll();
        if (claimRequestRepository != null) claimRequestRepository.deleteAll();
        mosqueRepository.deleteAll();
    }

    @Test
    @DisplayName("MOSQUE_ADMIN should create an event and public user can query it")
    void testCreateAndQueryEvent() {
        MosqueEventCreateDto dto = MosqueEventCreateDto.builder()
                .title("Tafseer of Surah Al-Mulk")
                .description("Weekly post-Isha reflection")
                .eventType(EventType.HALAQAH)
                .audience(EventAudience.ALL)
                .startDateTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endDateTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS))
                .speakerName("Shaykh Hassan")
                .build();

        MosqueEventResponseDto created = eventService.createEvent(testMosque.getId(), dto, mosqueAdmin);

        assertThat(created).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Tafseer of Surah Al-Mulk");
        assertThat(created.getMosqueId()).isEqualTo(testMosque.getId());

        Page<MosqueEventResponseDto> upcoming = eventService.getUpcomingEvents(
                testMosque.getSlug(), EventType.HALAQAH, PageRequest.of(0, 10));

        assertThat(upcoming.getContent()).hasSize(1);
        assertThat(upcoming.getContent().get(0).getTitle()).isEqualTo("Tafseer of Surah Al-Mulk");
    }

    @Test
    @DisplayName("Regular user should be forbidden from creating events")
    void testRegularUserCannotCreateEvent() {
        MosqueEventCreateDto dto = MosqueEventCreateDto.builder()
                .title("Unauthorized Event")
                .eventType(EventType.OTHER)
                .startDateTime(Instant.now().plus(1, ChronoUnit.DAYS))
                .endDateTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .build();

        assertThatThrownBy(() -> eventService.createEvent(testMosque.getId(), dto, regularUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("MOSQUE_ADMIN should publish Friday Khutbah and query upcoming schedule")
    void testPublishAndQueryKhutbah() {
        MosqueKhutbahCreateDto dto = MosqueKhutbahCreateDto.builder()
                .khutbahDate(LocalDate.now().plusDays(2))
                .topic("Preserving the Family Unit")
                .khatibName("Imam Hassan")
                .batchNumber(1)
                .khutbahTime(LocalTime.of(13, 0))
                .adhaanTime(LocalTime.of(12, 45))
                .iqamahTime(LocalTime.of(13, 30))
                .language("English & Arabic")
                .streamUrl("https://youtube.com/live/demo")
                .build();

        MosqueKhutbahResponseDto created = khutbahService.createKhutbah(testMosque.getId(), dto, mosqueAdmin);

        assertThat(created).isNotNull();
        assertThat(created.getTopic()).isEqualTo("Preserving the Family Unit");

        List<MosqueKhutbahResponseDto> upcoming = khutbahService.getUpcomingKhutbahs(testMosque.getSlug());
        assertThat(upcoming).hasSize(1);
        assertThat(upcoming.get(0).getKhatibName()).isEqualTo("Imam Hassan");
    }
}
