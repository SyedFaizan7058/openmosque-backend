package com.openmosque.modules.mosque.repository;

import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.entity.MosqueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Mosque entities with PostGIS Spatial Query capabilities.
 */
@Repository
public interface MosqueRepository extends JpaRepository<Mosque, UUID> {

    Optional<Mosque> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Optional<Mosque> findByIdAndDeletedFalse(UUID id);

    Optional<Mosque> findBySlugAndDeletedFalse(String slug);

    List<Mosque> findByStatusAndDeletedFalse(MosqueStatus status);

    /**
     * Finds active mosques within a radius (in meters) from a GPS coordinate,
     * ordered by distance ascending.
     * 
     * Uses PostGIS 'ST_DWithin' on geography cast for accurate spherical earth distance in PostgreSQL.
     */
    @Query(value = """
        SELECT m.*
        FROM mosques m
        WHERE m.status = 'ACTIVE' 
          AND m.is_deleted = false
          AND ST_DWithin(
                m.location::geography, 
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 
                :radiusMeters
              )
        ORDER BY ST_DistanceSphere(m.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) ASC
        """, nativeQuery = true)
    List<Mosque> findNearbyMosques(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters
    );

    /**
     * Filtered search query across mosque name, address, city, and country with pagination.
     */
    @Query("""
        SELECT m FROM Mosque m
        WHERE m.status = :status
          AND m.deleted = false
          AND (CAST(:query AS String) IS NULL OR 
               LOWER(m.name) LIKE LOWER(CONCAT('%', CAST(:query AS String), '%')) 
               OR LOWER(m.address) LIKE LOWER(CONCAT('%', CAST(:query AS String), '%'))
               OR LOWER(m.city) LIKE LOWER(CONCAT('%', CAST(:query AS String), '%'))
               OR LOWER(m.state) LIKE LOWER(CONCAT('%', CAST(:query AS String), '%')))
          AND (CAST(:city AS String) IS NULL OR LOWER(m.city) = LOWER(CAST(:city AS String)))
          AND (CAST(:country AS String) IS NULL OR LOWER(m.country) = LOWER(CAST(:country AS String)))
        """)
    Page<Mosque> searchMosques(
            @Param("query") String query,
            @Param("city") String city,
            @Param("country") String country,
            @Param("status") MosqueStatus status,
            Pageable pageable
    );

    long countByDeletedFalse();

    long countByVerifiedTrueAndDeletedFalse();
}
