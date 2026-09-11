package com.openmosque.modules.notification.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.notification.dto.DeviceTokenRegisterDto;
import com.openmosque.modules.notification.dto.DeviceTokenResponseDto;
import com.openmosque.modules.notification.dto.NotificationResponseDto;
import com.openmosque.modules.notification.dto.NotificationSummaryDto;
import com.openmosque.modules.notification.service.NotificationService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "User Notifications & Push Alerts", description = "Endpoints for managing notifications, push alerts, and FCM device tokens")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get user notifications", description = "Retrieves paginated notifications for current authenticated user.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponseDto>>> getNotifications(
            @CurrentUser User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<NotificationResponseDto> response = notificationService.getUserNotifications(user, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get unread notifications count", description = "Returns total count of unread notifications for notification bell badge.")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<NotificationSummaryDto>> getUnreadCount(
            @CurrentUser User user
    ) {
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(ApiResponse.success(new NotificationSummaryDto(count)));
    }

    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read.")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @CurrentUser User user,
            @PathVariable UUID id
    ) {
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications for current user as read.")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(
            @CurrentUser User user
    ) {
        int updated = notificationService.markAllAsRead(user);
        return ResponseEntity.ok(ApiResponse.success(updated, "All notifications marked as read"));
    }

    // --- Push Notification Device Token Registry Endpoints ---

    @Operation(summary = "Register FCM device token", description = "Registers or updates a device push notification token for current user.")
    @PostMapping("/devices")
    public ResponseEntity<ApiResponse<DeviceTokenResponseDto>> registerDevice(
            @CurrentUser User user,
            @Valid @RequestBody DeviceTokenRegisterDto dto
    ) {
        DeviceTokenResponseDto response = notificationService.registerDevice(user, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Device registered for push notifications successfully"));
    }

    @Operation(summary = "Unregister FCM device token", description = "Removes a device push notification token on logout.")
    @DeleteMapping("/devices/{fcmToken}")
    public ResponseEntity<ApiResponse<Void>> unregisterDevice(
            @CurrentUser User user,
            @PathVariable String fcmToken
    ) {
        notificationService.unregisterDevice(user, fcmToken);
        return ResponseEntity.ok(ApiResponse.success(null, "Device unregistered successfully"));
    }

    @Operation(summary = "Get user devices", description = "Lists active registered devices for current user.")
    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<DeviceTokenResponseDto>>> getUserDevices(
            @CurrentUser User user
    ) {
        List<DeviceTokenResponseDto> devices = notificationService.getUserDevices(user);
        return ResponseEntity.ok(ApiResponse.success(devices, "User devices retrieved successfully"));
    }
}
