package com.openmosque;

import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.media.service.StorageService;
import com.openmosque.modules.moderation.dto.PlatformStatsDto;
import com.openmosque.modules.mosque.dto.MosqueCreateRequestDto;
import com.openmosque.modules.mosque.dto.MosqueResponseDto;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.mosque.repository.UserFavoriteMosqueRepository;
import com.openmosque.modules.mosque.service.MosqueFavoriteService;
import com.openmosque.modules.mosque.service.MosqueService;
import com.openmosque.modules.mosque.service.StatsService;
import com.openmosque.modules.notification.dto.NotificationResponseDto;
import com.openmosque.modules.notification.entity.NotificationType;
import com.openmosque.modules.notification.entity.UserNotification;
import com.openmosque.modules.notification.repository.UserNotificationRepository;
import com.openmosque.modules.notification.service.NotificationService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationAndMediaTests {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserNotificationRepository notificationRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private MosqueFavoriteService favoriteService;

    @Autowired
    private UserFavoriteMosqueRepository favoriteRepository;

    @Autowired
    private MosqueService mosqueService;

    @Autowired
    private MosqueRepository mosqueRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatsService statsService;

    @Autowired
    private com.openmosque.modules.user.repository.UserBadgeRepository userBadgeRepository;

    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        favoriteRepository.deleteAll();
        userBadgeRepository.deleteAll();

        testUser = userRepository.save(User.builder()
                .firebaseUid("test-notif-user-" + UUID.randomUUID())
                .email("notif.user." + UUID.randomUUID() + "@openmosque.test")
                .displayName("Notification Tester")
                .role(UserRole.USER)
                .points(0)
                .build());

        testAdmin = userRepository.save(User.builder()
                .firebaseUid("test-notif-admin-" + UUID.randomUUID())
                .email("notif.admin." + UUID.randomUUID() + "@openmosque.test")
                .displayName("Admin Tester")
                .role(UserRole.SUPER_ADMIN)
                .points(100)
                .build());
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        favoriteRepository.deleteAll();
        userBadgeRepository.deleteAll();
        userRepository.delete(testUser);
        userRepository.delete(testAdmin);
    }

    @Test
    void testCreateAndQueryUserNotifications() {
        // 1. Initially 0 unread notifications
        long initialCount = notificationService.getUnreadCount(testUser);
        assertEquals(0, initialCount);

        // 2. Dispatch a notification
        UserNotification notif = notificationService.notifyUser(
                testUser,
                "Welcome to OpenMosque",
                "Your account is ready.",
                NotificationType.SYSTEM_ANNOUNCEMENT,
                "/home",
                null
        );
        assertNotNull(notif.getId());

        // 3. Unread count increments to 1
        assertEquals(1, notificationService.getUnreadCount(testUser));

        // 4. Retrieve paginated list
        PageResponse<NotificationResponseDto> list = notificationService.getUserNotifications(
                testUser, PageRequest.of(0, 10));
        assertEquals(1, list.getContent().size());
        assertEquals("Welcome to OpenMosque", list.getContent().get(0).getTitle());
        assertFalse(list.getContent().get(0).isRead());

        // 5. Mark as read
        NotificationResponseDto updated = notificationService.markAsRead(notif.getId(), testUser);
        assertTrue(updated.isRead());
        assertEquals(0, notificationService.getUnreadCount(testUser));
    }

    @Test
    void testIqamahChangeNotificationToFavoriters() {
        // 1. Create a test mosque
        MosqueResponseDto mosque = mosqueService.createMosque(MosqueCreateRequestDto.builder()
                .name("Notification Test Mosque " + UUID.randomUUID())
                .address("10 Prayer Lane")
                .city("London")
                .country("United Kingdom")
                .latitude(51.5)
                .longitude(-0.1)
                .build(), testAdmin);

        // 2. User favorites the mosque (may trigger DEVOTED_PATRON badge notification if badge exists)
        favoriteService.addFavorite(testUser, mosque.getId());
        long unreadBefore = notificationService.getUnreadCount(testUser);

        // 3. Dispatch Iqamah change notification
        int notifiedCount = notificationService.notifyMosqueFavoriters(
                mosque.getId(),
                "Iqamah Changed",
                "Asr moved to 17:30",
                NotificationType.IQAMAH_CHANGE,
                "/mosques/" + mosque.getSlug(),
                null
        );
        assertEquals(1, notifiedCount);

        // 4. Verify user received the notification (unread incremented by 1)
        assertEquals(unreadBefore + 1, notificationService.getUnreadCount(testUser));
        PageResponse<NotificationResponseDto> page = notificationService.getUserNotifications(
                testUser, PageRequest.of(0, 10));
        assertTrue(page.getContent().stream().anyMatch(n -> "Iqamah Changed".equals(n.getTitle())));

        // Cleanup
        favoriteRepository.deleteAll();
        mosqueRepository.deleteById(mosque.getId());
    }

    @Test
    void testMediaLocalStorageSaveAndServe() {
        byte[] testData = "Fake PNG binary image data for testing".getBytes();
        String publicUrl = storageService.storeFile("test-mosque", "sample.png", testData, "image/png");

        assertNotNull(publicUrl);
        assertTrue(publicUrl.contains("/media/files/test-mosque/"));

        // Delete the saved file
        boolean deleted = storageService.deleteFile(publicUrl);
        assertTrue(deleted);
    }

    @Test
    void testPlatformStats() {
        PlatformStatsDto stats = statsService.getPlatformStats();
        assertNotNull(stats);
        assertTrue(stats.getTotalUsers() >= 2);
    }
}
