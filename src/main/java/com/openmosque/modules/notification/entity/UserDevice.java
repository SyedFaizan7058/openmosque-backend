package com.openmosque.modules.notification.entity;

import com.openmosque.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", nullable = false, unique = true, length = 500)
    private String fcmToken;

    @Column(name = "device_type", nullable = false, length = 50)
    @Builder.Default
    private String deviceType = "WEB"; // WEB, ANDROID, IOS

    @Column(name = "device_name", length = 150)
    private String deviceName;

    @Column(name = "last_active_at", nullable = false)
    @Builder.Default
    private Instant lastActiveAt = Instant.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
