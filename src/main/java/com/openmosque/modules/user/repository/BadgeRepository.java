package com.openmosque.modules.user.repository;

import com.openmosque.modules.user.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, UUID> {
    Optional<Badge> findByCodeAndActiveTrue(String code);
    List<Badge> findByActiveTrueOrderByCreatedAtAsc();
}
