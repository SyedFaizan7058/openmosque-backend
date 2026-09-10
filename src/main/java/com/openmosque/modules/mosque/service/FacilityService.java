package com.openmosque.modules.mosque.service;

import com.openmosque.modules.mosque.dto.FacilityDto;
import com.openmosque.modules.mosque.entity.Facility;
import com.openmosque.modules.mosque.mapper.MosqueMapper;
import com.openmosque.modules.mosque.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing Standard Mosque Facilities and Amenities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final MosqueMapper mosqueMapper;

    /**
     * Retrieves all active standard facilities (Wudu, Parking, Women's Section, etc.).
     */
    @Transactional(readOnly = true)
    public List<FacilityDto> getAllActiveFacilities() {
        List<Facility> facilities = facilityRepository.findByActiveTrue();
        return mosqueMapper.toFacilityDtoList(facilities);
    }
}
