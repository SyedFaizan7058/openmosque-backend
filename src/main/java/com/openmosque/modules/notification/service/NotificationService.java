package com.openmosque.modules.notification.service;

import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.mosque.entity.UserFavoriteMosque;
import com.openmosque.modules.mosque.repository.UserFavoriteMosqueRepository;
import com.openmosque.modules.notification.dto.NotificationResponseDto;
import com.openmosque.modules.notification.entity.NotificationType;
import com.openmosque.modules.notification.entity.UserNotification;
import com.openmosque.modules.notification.repository.UserNotificationRepository;
import com.openmosque.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserNotificationRepository notificationRepository;
    private final UserFavoriteMosqueRepository favoriteRepository;

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
        log.info("Dispatched notification [{}] to user '{}' (ID: {})", type, user.getEmail(), user.getId());
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
        }

        notificationRepository.saveAll(notifications);
        log.info("Dispatched broadcast notification [{}] for mosque ID {} to {} worshippers",
                type, mosqueId, notifications.size());
        return notifications.size();
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponseDto> getUserNotifications(User user, Pageable pageable) {
        Page<UserNotification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        List<NotificationResponseDto> content = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return PageResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public NotificationResponseDto markAsRead(UUID notificationId, User user) {
        UserNotification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notification.setRead(true);
        UserNotification updated = notificationRepository.save(notification);
        return toDto(updated);
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
