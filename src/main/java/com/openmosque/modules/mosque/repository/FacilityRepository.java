package com.openmosque.modules.mosque.repository;

import com.openmosque.modules.mosque.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, UUID> {

    Optional<Facility> findByCode(String code);

    List<Facility> findByCodeIn(List<String> codes);

    List<Facility> findByActiveTrue();
}
