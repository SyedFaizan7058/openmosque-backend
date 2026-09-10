package com.openmosque.modules.mosque.repository;

import com.openmosque.modules.mosque.entity.UserFavoriteMosque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFavoriteMosqueRepository extends JpaRepository<UserFavoriteMosque, UUID> {

    boolean existsByUserIdAndMosqueId(UUID userId, UUID mosqueId);

    long countByUserId(UUID userId);

    long countByMosqueId(UUID mosqueId);

    @Query("SELECT f FROM UserFavoriteMosque f JOIN FETCH f.mosque m WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<UserFavoriteMosque> findAllByUserIdWithMosque(@Param("userId") UUID userId);

    @Query("SELECT f FROM UserFavoriteMosque f JOIN FETCH f.user u WHERE f.mosque.id = :mosqueId")
    List<UserFavoriteMosque> findByMosqueIdWithUser(@Param("mosqueId") UUID mosqueId);

    Optional<UserFavoriteMosque> findByUserIdAndMosqueId(UUID userId, UUID mosqueId);

    @Modifying
    @Query("DELETE FROM UserFavoriteMosque f WHERE f.user.id = :userId AND f.mosque.id = :mosqueId")
    int deleteByUserIdAndMosqueId(@Param("userId") UUID userId, @Param("mosqueId") UUID mosqueId);
}
