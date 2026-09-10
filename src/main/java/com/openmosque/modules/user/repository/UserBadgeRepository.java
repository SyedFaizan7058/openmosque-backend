package com.openmosque.modules.user.repository;

import com.openmosque.modules.user.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UUID> {

    boolean existsByUserIdAndBadgeId(UUID userId, UUID badgeId);

    @Query("SELECT CASE WHEN COUNT(ub) > 0 THEN TRUE ELSE FALSE END FROM UserBadge ub WHERE ub.user.id = :userId AND ub.badge.code = :badgeCode")
    boolean existsByUserIdAndBadgeCode(@Param("userId") UUID userId, @Param("badgeCode") String badgeCode);

    @Query("SELECT ub FROM UserBadge ub JOIN FETCH ub.badge WHERE ub.user.id = :userId ORDER BY ub.earnedAt DESC")
    List<UserBadge> findAllByUserIdWithBadge(@Param("userId") UUID userId);
}
