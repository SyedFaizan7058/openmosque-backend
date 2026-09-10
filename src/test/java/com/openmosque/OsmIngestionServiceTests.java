package com.openmosque;

import com.openmosque.common.exception.ForbiddenException;
import com.openmosque.common.util.GeoUtils;
import com.openmosque.modules.ingestion.client.OverpassApiClient;
import com.openmosque.modules.ingestion.controller.OsmIngestionController;
import com.openmosque.modules.ingestion.dto.CityIngestRequestDto;
import com.openmosque.modules.ingestion.dto.IngestionSummaryDto;
import com.openmosque.modules.ingestion.dto.OsmElementDto;
import com.openmosque.modules.ingestion.dto.OsmResponseDto;
import com.openmosque.modules.ingestion.service.OsmIngestionService;
import com.openmosque.modules.mosque.entity.Facility;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.entity.MosqueStatus;
import com.openmosque.modules.mosque.repository.FacilityRepository;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class OsmIngestionServiceTests {

    @Autowired
    private OsmIngestionService osmIngestionService;

    @Autowired
    private OsmIngestionController osmIngestionController;

    @MockBean
    private OverpassApiClient overpassApiClient;

    @Autowired
    private MosqueRepository mosqueRepository;

    @Autowired
    private FacilityRepository facilityRepository;

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
    private com.openmosque.modules.community.repository.MosqueQuestionRepository questionRepository;

    @Autowired(required = false)
    private com.openmosque.modules.community.repository.MosqueAnswerRepository answerRepository;

    @Autowired(required = false)
    private com.openmosque.modules.community.repository.CommunityContentFlagRepository flagRepository;

    @Autowired(required = false)
    private com.openmosque.modules.moderation.repository.MosqueSubmissionRepository submissionRepository;

    @Autowired(required = false)
    private com.openmosque.modules.claim.repository.MosqueClaimRequestRepository claimRepository;

    private User superAdmin;
    private User regularUser;

    @BeforeEach
    void setUp() {
        if (flagRepository != null) flagRepository.deleteAll();
        if (answerRepository != null) answerRepository.deleteAll();
        if (questionRepository != null) questionRepository.deleteAll();
        if (reviewRepository != null) reviewRepository.deleteAll();
        if (khutbahRepository != null) khutbahRepository.deleteAll();
        if (eventRepository != null) eventRepository.deleteAll();
        if (iqamahScheduleRepository != null) iqamahScheduleRepository.deleteAll();
        if (prayerConfigRepository != null) prayerConfigRepository.deleteAll();
        if (claimRepository != null) claimRepository.deleteAll();
        if (submissionRepository != null) submissionRepository.deleteAll();
        mosqueRepository.deleteAll();
        userRepository.deleteAll();

        // Ensure baseline facilities exist
        ensureFacility("WOMENS_SECTION", "Women's Section");
        ensureFacility("WHEELCHAIR_ACCESSIBILITY", "Wheelchair Accessible");
        ensureFacility("PARKING", "Parking");
        ensureFacility("WUDU_AREA", "Wudu Area");

        superAdmin = userRepository.save(User.builder()
                .email("superadmin-" + UUID.randomUUID() + "@openmosque.org")
                .displayName("Super Administrator")
                .role(UserRole.SUPER_ADMIN)
                .firebaseUid("fb-admin-" + UUID.randomUUID())
                .build());

        regularUser = userRepository.save(User.builder()
                .email("worshipper-" + UUID.randomUUID() + "@openmosque.org")
                .displayName("Regular Worshipper")
                .role(UserRole.USER)
                .firebaseUid("fb-user-" + UUID.randomUUID())
                .build());
    }

    private void ensureFacility(String code, String name) {
        if (facilityRepository.findByCode(code).isEmpty()) {
            facilityRepository.save(Facility.builder()
                    .code(code)
                    .name(name)
                    .description(name)
                    .iconName("icon-" + code.toLowerCase())
                    .active(true)
                    .build());
        }
    }

    @Test
    @Transactional
    @DisplayName("Should successfully ingest OSM nodes and ways with tags and coordinates")
    void testIngestByCity_successWithFacilitiesAndCoordinates() {
        // Given: Overpass returns 1 node and 1 way
        OsmElementDto nodeElement = OsmElementDto.builder()
                .type("node")
                .id(1001L)
                .lat(52.201)
                .lon(0.131)
                .tags(Map.of(
                        "amenity", "place_of_worship",
                        "religion", "muslim",
                        "name", "Cambridge Islamic Centre",
                        "addr:street", "Mill Road",
                        "addr:city", "Cambridge",
                        "female", "yes",
                        "wheelchair", "yes",
                        "parking", "yes"
                ))
                .build();

        OsmElementDto wayElement = OsmElementDto.builder()
                .type("way")
                .id(2002L)
                .center(new OsmElementDto.OsmCenterDto(52.205, 0.135))
                .tags(Map.of(
                        "amenity", "place_of_worship",
                        "religion", "muslim",
                        "name", "Abu Bakr Mosque",
                        "addr:street", "Mawson Road",
                        "addr:city", "Cambridge",
                        "female", "separate"
                ))
                .build();

        when(overpassApiClient.fetchByCity(anyString(), anyString()))
                .thenReturn(new OsmResponseDto(0.6, "Overpass", List.of(nodeElement, wayElement)));

        CityIngestRequestDto request = CityIngestRequestDto.builder()
                .city("Cambridge")
                .country("United Kingdom")
                .dryRun(false)
                .build();

        // When
        IngestionSummaryDto summary = osmIngestionService.ingestByCity(request, superAdmin);

        // Then
        assertThat(summary.getTotalElementsFetched()).isEqualTo(2);
        assertThat(summary.getMosquesInserted()).isEqualTo(2);
        assertThat(summary.getDuplicatesSkipped()).isEqualTo(0);
        assertThat(summary.getFacilitiesAttached()).isGreaterThanOrEqualTo(4);
        assertThat(summary.getInsertedMosqueNames()).contains("Cambridge Islamic Centre", "Abu Bakr Mosque");

        // Verify entities in database
        List<Mosque> mosques = mosqueRepository.findAll();
        assertThat(mosques).hasSize(2);

        Mosque cambridge = mosques.stream()
                .filter(m -> m.getName().equals("Cambridge Islamic Centre"))
                .findFirst().orElseThrow();
        assertThat(cambridge.getLatitude()).isEqualTo(52.201);
        assertThat(cambridge.getLongitude()).isEqualTo(0.131);
        assertThat(cambridge.getAddress()).contains("Mill Road");
        assertThat(cambridge.getFacilities()).isNotEmpty();
    }

    @Test
    @DisplayName("Should detect and skip duplicate mosques within 50m spatial proximity")
    void testSpatialDeduplication_skipsWithin50Meters() {
        // Given: A mosque already exists in the database
        mosqueRepository.save(Mosque.builder()
                .name("Existing Cambridge Mosque")
                .slug("existing-cambridge-mosque")
                .address("123 Mill Road")
                .city("Cambridge")
                .country("UK")
                .latitude(52.20100)
                .longitude(0.13100)
                .location(GeoUtils.createPoint(52.20100, 0.13100))
                .status(MosqueStatus.ACTIVE)
                .verified(true)
                .build());

        // OSM returns a node only ~15 meters away
        OsmElementDto duplicateNode = OsmElementDto.builder()
                .type("node")
                .id(3003L)
                .lat(52.20108) // ~9 meters away in latitude
                .lon(0.13105)
                .tags(Map.of(
                        "amenity", "place_of_worship",
                        "religion", "muslim",
                        "name", "Cambridge Mosque (OSM)"
                ))
                .build();

        when(overpassApiClient.fetchByCity(anyString(), anyString()))
                .thenReturn(new OsmResponseDto(0.6, "Overpass", List.of(duplicateNode)));

        CityIngestRequestDto request = CityIngestRequestDto.builder()
                .city("Cambridge")
                .country("UK")
                .dryRun(false)
                .build();

        // When
        IngestionSummaryDto summary = osmIngestionService.ingestByCity(request, superAdmin);

        // Then
        assertThat(summary.getTotalElementsFetched()).isEqualTo(1);
        assertThat(summary.getMosquesInserted()).isEqualTo(0);
        assertThat(summary.getDuplicatesSkipped()).isEqualTo(1);
        assertThat(mosqueRepository.count()).isEqualTo(1); // No new mosque added
    }

    @Test
    @DisplayName("Dry-run mode should compute metrics without writing changes to the database")
    void testDryRun_doesNotPersistToDatabase() {
        OsmElementDto nodeElement = OsmElementDto.builder()
                .type("node")
                .id(4004L)
                .lat(43.653)
                .lon(-79.383)
                .tags(Map.of(
                        "name", "Toronto Downtown Masjid",
                        "addr:city", "Toronto"
                ))
                .build();

        when(overpassApiClient.fetchByCity(anyString(), anyString()))
                .thenReturn(new OsmResponseDto(0.6, "Overpass", List.of(nodeElement)));

        CityIngestRequestDto request = CityIngestRequestDto.builder()
                .city("Toronto")
                .country("Canada")
                .dryRun(true)
                .build();

        // When
        IngestionSummaryDto summary = osmIngestionService.ingestByCity(request, superAdmin);

        // Then
        assertThat(summary.isDryRun()).isTrue();
        assertThat(summary.getMosquesInserted()).isEqualTo(1);
        assertThat(summary.getInsertedMosqueNames()).contains("Toronto Downtown Masjid");

        // Assert database was NOT touched
        assertThat(mosqueRepository.count()).isZero();
    }

    @Test
    @DisplayName("RBAC: Regular worshipper should be forbidden from triggering OSM ingestion")
    void testRBAC_forbiddenForRegularUser() {
        CityIngestRequestDto request = CityIngestRequestDto.builder()
                .city("London")
                .build();

        assertThatThrownBy(() -> osmIngestionController.ingestByCity(request, regularUser))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }
}
