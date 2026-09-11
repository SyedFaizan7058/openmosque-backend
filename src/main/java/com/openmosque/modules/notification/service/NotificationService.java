package com.openmosque.modules.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.mosque.entity.UserFavoriteMosque;
import com.openmosque.modules.mosque.repository.UserFavoriteMosqueRepository;
import com.openmosque.modules.notification.dto.DeviceTokenRegisterDto;
import com.openmosque.modules.notification.dto.DeviceTokenResponseDto;
import com.openmosque.modules.notification.dto.NotificationResponseDto;
import com.openmosque.modules.notification.entity.NotificationType;
import com.openmosque.modules.notification.entity.UserDevice;
import com.openmosque.modules.notification.entity.UserNotification;
import com.openmosque.modules.notification.repository.UserDeviceRepository;
import com.openmosque.modules.notification.repository.UserNotificationRepository;
import com.openmosque.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserNotificationRepository notificationRepository;
    private final UserFavoriteMosqueRepository favoriteRepository;
    private final UserDeviceRepository userDeviceRepository;

    // --- Device Token Management ---

    @Transactional
    public DeviceTokenResponseDto registerDevice(User user, DeviceTokenRegisterDto dto) {
        String token = dto.getFcmToken().trim();

        // Check if token already registered (either by this user or another previous user of device)
        Optional<UserDevice> existingOpt = userDeviceRepository.findByFcmToken(token);
        UserDevice device;
        if (existingOpt.isPresent()) {
            device = existingOpt.get();
            device.setUser(user);
            device.setDeviceType(dto.getDeviceType() != null ? dto.getDeviceType() : "WEB");
            if (dto.getDeviceName() != null) {
                device.setDeviceName(dto.getDeviceName());
            }
            device.setLastActiveAt(Instant.now());
        } else {
            device = UserDevice.builder()
                    .user(user)
                    .fcmToken(token)
                    .deviceType(dto.getDeviceType() != null ? dto.getDeviceType() : "WEB")
                    .deviceName(dto.getDeviceName())
                    .lastActiveAt(Instant.now())
                    .build();
        }

        UserDevice saved = userDeviceRepository.save(device);
        log.info("Registered FCM device token for user '{}' (type: {}, device: {})",
                user.getEmail(), saved.getDeviceType(), saved.getDeviceName());

        return DeviceTokenResponseDto.builder()
                .id(saved.getId())
                .fcmToken(saved.getFcmToken())
                .deviceType(saved.getDeviceType())
                .deviceName(saved.getDeviceName())
                .lastActiveAt(saved.getLastActiveAt())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public void unregisterDevice(User user, String fcmToken) {
        userDeviceRepository.deleteByUserIdAndFcmToken(user.getId(), fcmToken.trim());
        log.info("Unregistered FCM device token for user '{}'", user.getEmail());
    }

    @Transactional(readOnly = true)
    public List<DeviceTokenResponseDto> getUserDevices(User user) {
        return userDeviceRepository.findByUserId(user.getId()).stream()
                .map(d -> DeviceTokenResponseDto.builder()
                        .id(d.getId())
                        .fcmToken(d.getFcmToken())
                        .deviceType(d.getDeviceType())
                        .deviceName(d.getDeviceName())
                        .lastActiveAt(d.getLastActiveAt())
                        .createdAt(d.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // --- Notification Dispatch ---

    @Transactional
    public UserNotification notifyUser(User user, String title, String message,
                                       NotificationType type, String linkUrl, String metadataJson) {
        UserNotification notification = UserNotification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .linkUrl(linkUrl)
                .metadataJson(metadataJson)
                .read(false)
                .build();
        UserNotification saved = notificationRepository.save(notification);
        log.info("Dispatched in-app notification [{}] to user '{}' (ID: {})", type, user.getEmail(), user.getId());

        // Dispatch FCM Push Notification to all user's registered devices
        try {
            List<UserDevice> devices = userDeviceRepository.findByUserId(user.getId());
            if (!devices.isEmpty()) {
                List<String> tokens = devices.stream().map(UserDevice::getFcmToken).toList();
                sendFcmPush(tokens, title, message, type, linkUrl, metadataJson);
            }
        } catch (Exception ex) {
            log.warn("Could not dispatch FCM push notification to user '{}': {}", user.getEmail(), ex.getMessage());
        }

        return saved;
    }

    @Transactional
    public int notifyMosqueFavoriters(UUID mosqueId, String title, String message,
                                      NotificationType type, String linkUrl, String metadataJson) {
        List<UserFavoriteMosque> favorites = favoriteRepository.findByMosqueIdWithUser(mosqueId);
        if (favorites.isEmpty()) {
            return 0;
        }

        List<UserNotification> notifications = new ArrayList<>();
        List<UUID> userIds = new ArrayList<>();
        for (UserFavoriteMosque fav : favorites) {
            notifications.add(UserNotification.builder()
                    .user(fav.getUser())
                    .title(title)
                    .message(message)
                    .type(type)
                    .linkUrl(linkUrl)
                    .metadataJson(metadataJson)
                    .read(false)
                    .build());
            userIds.add(fav.getUser().getId());
        }

        notificationRepository.saveAll(notifications);
        log.info("Dispatched broadcast notification [{}] for mosque ID {} to {} worshippers",
                type, mosqueId, notifications.size());

        // Dispatch FCM Push Notifications in batches to all device tokens of favoriters
        try {
            List<UserDevice> devices = userDeviceRepository.findByUserIdIn(userIds);
            if (!devices.isEmpty()) {
                List<String> tokens = devices.stream().map(UserDevice::getFcmToken).distinct().toList();
                sendFcmPush(tokens, title, message, type, linkUrl, metadataJson);
            }
        } catch (Exception ex) {
            log.warn("Could not dispatch broadcast FCM push notifications for mosque {}: {}", mosqueId, ex.getMessage());
        }

        return notifications.size();
    }

    /**
     * Dispatches real FCM push notifications with batching and automatic stale token cleanup.
     */
    private void sendFcmPush(List<String> tokens, String title, String body,
                             NotificationType type, String linkUrl, String metadataJson) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.debug("FirebaseApp is not initialized. Skipping FCM push send for {} recipients.", tokens.size());
            return;
        }

        // Firebase sendEachForMulticast accepts up to 500 tokens per call
        int batchSize = 500;
        for (int i = 0; i < tokens.size(); i += batchSize) {
            List<String> batch = tokens.subList(i, Math.min(i + batchSize, tokens.size()));
            try {
                MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                        .setNotification(com.google.firebase.messaging.Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putData("type", type != null ? type.name() : "GENERAL")
                        .putData("click_action", "FLUTTER_NOTIFICATION_CLICK");

                if (linkUrl != null) messageBuilder.putData("linkUrl", linkUrl);
                if (metadataJson != null) messageBuilder.putData("metadata", metadataJson);

                messageBuilder.addAllTokens(batch);
                MulticastMessage multicastMessage = messageBuilder.build();

                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage);
                log.info("FCM multicast sent: {} successes, {} failures out of {} tokens.",
                        response.getSuccessCount(), response.getFailureCount(), batch.size());

                // Auto-cleanup invalid/expired/unregistered tokens
                if (response.getFailureCount() > 0) {
                    List<String> tokensToPurge = new ArrayList<>();
                    List<SendResponse> responses = response.getResponses();
                    for (int j = 0; j < responses.size(); j++) {
                        SendResponse sr = responses.get(j);
                        if (!sr.isSuccessful()) {
                            FirebaseMessagingException ex = (FirebaseMessagingException) sr.getException();
                            if (ex != null) {
                                MessagingErrorCode code = ex.getMessagingErrorCode();
                                if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                                    tokensToPurge.add(batch.get(j));
                                }
                            }
                        }
                    }

                    if (!tokensToPurge.isEmpty()) {
                        userDeviceRepository.deleteByFcmTokenIn(tokensToPurge);
                        log.info("Cleaned up {} unregistered/invalid FCM device tokens from database.", tokensToPurge.size());
                    }
                }
            } catch (Exception e) {
                log.warn("FCM push batch dispatch failed: {}", e.getMessage());
            }
        }
    }

    // --- Query & In-App Notification Methods ---

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponseDto> getUserNotifications(User user, Pageable pageable) {
        Page<UserNotification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        List<NotificationResponseDto> dtoList = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return PageResponse.from(page, dtoList);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public NotificationResponseDto markAsRead(UUID notificationId, User user) {
        UserNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("UserNotification", "id", notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new com.openmosque.common.exception.ForbiddenException("Cannot mark another user's notification as read.");
        }

        notification.setRead(true);
        UserNotification saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    @Transactional
    public int markAllAsRead(User user) {
        return notificationRepository.markAllAsRead(user.getId());
    }

    private NotificationResponseDto toDto(UserNotification n) {
        return NotificationResponseDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .linkUrl(n.getLinkUrl())
                .metadataJson(n.getMetadataJson())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
