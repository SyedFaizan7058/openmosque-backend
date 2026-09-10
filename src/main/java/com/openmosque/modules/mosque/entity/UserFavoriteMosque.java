package com.openmosque.modules.mosque.entity;

import com.openmosque.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity mapping user's bookmarked / favorited mosques.
 * Uses hard deletion by design to preserve unique constraint integrity.
 */
@Entity
@Table(name = "user_favorite_mosques", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_favorite_mosque", columnNames = {"user_id", "mosque_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteMosque {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mosque_id", nullable = false)
    private Mosque mosque;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
