package com.openmosque.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTokenRegisterDto {

    @NotBlank(message = "FCM device token cannot be blank")
    @Size(max = 500, message = "FCM device token exceeds max length")
    private String fcmToken;

    @Builder.Default
    private String deviceType = "WEB"; // WEB, ANDROID, IOS

    @Size(max = 150, message = "Device name exceeds max length")
    private String deviceName;
}
