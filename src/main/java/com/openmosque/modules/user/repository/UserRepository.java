package com.openmosque.modules.user.repository;

import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    Optional<User> findByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);

    Page<User> findByRole(UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE (:role IS NULL OR u.role = :role) " +
           "AND (:search IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR (u.displayName IS NOT NULL AND LOWER(u.displayName) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<User> searchUsers(
            @Param("role") UserRole role,
            @Param("search") String search,
            Pageable pageable
    );

    long countByDeletedFalse();
}
