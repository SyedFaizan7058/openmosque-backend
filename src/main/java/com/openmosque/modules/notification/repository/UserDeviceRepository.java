package com.openmosque.modules.notification.repository;

import com.openmosque.modules.notification.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {

    Optional<UserDevice> findByFcmToken(String fcmToken);

    List<UserDevice> findByUserId(UUID userId);

    @Query("SELECT d FROM UserDevice d WHERE d.user.id IN :userIds")
    List<UserDevice> findByUserIdIn(@Param("userIds") Collection<UUID> userIds);

    @Modifying
    @Query("DELETE FROM UserDevice d WHERE d.fcmToken = :token")
    void deleteByFcmToken(@Param("token") String token);

    @Modifying
    @Query("DELETE FROM UserDevice d WHERE d.user.id = :userId AND d.fcmToken = :token")
    void deleteByUserIdAndFcmToken(@Param("userId") UUID userId, @Param("token") String token);

    @Modifying
    @Query("DELETE FROM UserDevice d WHERE d.fcmToken IN :tokens")
    void deleteByFcmTokenIn(@Param("tokens") Collection<String> tokens);
}
