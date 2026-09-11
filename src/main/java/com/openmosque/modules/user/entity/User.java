package com.openmosque.modules.user.entity;

import com.openmosque.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * User JPA Entity mapped to the 'users' PostgreSQL table.
 * 
 * WHY THIS IS PRESENT:
 * Stores registered user profiles, roles, and gamification contribution points.
 * 
 * PASSWORD STORAGE NOTE:
 * Notice there is NO 'password' column here. OpenMosque uses Firebase Authentication.
 * Passwords are encrypted, salted, and hashed using Google Scrypt/Bcrypt on Firebase's infrastructure.
 * The backend only maps and validates the secure 'firebase_uid' token claim.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    /**
     * Unique Identifier provided by Firebase Authentication.
     */
    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    private String firebaseUid;

    /**
     * User's primary email address (unique across platform).
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * User's visible display name or nickname.
     */
    @Column(name = "display_name", length = 150)
    private String displayName;

    /**
     * Contact phone number (optional).
     */
    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    /**
     * Profile photo URL hosted on GCP Cloud Storage.
     */
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    /**
     * System authorization role: USER, MOSQUE_ADMIN, MODERATOR, SUPER_ADMIN.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    @Builder.Default
    private UserRole role = UserRole.USER;

    /**
     * Crowdsourcing reward points earned by submitting or updating mosque information.
     */
    @Column(name = "points", nullable = false)
    @Builder.Default
    private int points = 0;

    /**
     * Account active status: 'false' if banned/suspended by moderators.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Mosque Administrator / Contributor verification flag.
     */
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    /**
     * User's preferred or detected home city (e.g. Pune, London).
     */
    @Column(name = "preferred_city", length = 100)
    private String preferredCity;

    /**
     * User's preferred country (e.g. India, United Kingdom).
     */
    @Column(name = "preferred_country", length = 100)
    private String preferredCountry;

    /**
     * User's preferred latitude coordinate.
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * User's preferred longitude coordinate.
     */
    @Column(name = "longitude")
    private Double longitude;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<com.openmosque.modules.mosque.entity.UserFavoriteMosque> favorites = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<UserBadge> badges = new java.util.ArrayList<>();

    /**
     * Whether Two-Factor Authentication (TOTP) is enabled.
     */
    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private boolean twoFactorEnabled = false;

    /**
     * Confirmed TOTP secret key (Base32 encoded).
     */
    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    /**
     * Pending temporary TOTP secret during enrollment verification.
     */
    @Column(name = "two_factor_temp_secret")
    private String twoFactorTempSecret;

    /**
     * Comma-separated or JSON list of BCrypt/SHA-256 hashed one-time recovery backup codes.
     */
    @Column(name = "two_factor_backup_codes", columnDefinition = "TEXT")
    private String twoFactorBackupCodes;

    /**
     * Helper method to increment user contribution points.
     */
    public void addPoints(int amount) {
        this.points += amount;
    }
}
