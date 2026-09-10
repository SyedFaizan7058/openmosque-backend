package com.openmosque.modules.event.service;

import com.openmosque.common.exception.BadRequestException;
import com.openmosque.common.exception.ConflictException;
import com.openmosque.common.exception.ForbiddenException;
import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.common.util.SecurityUtils;
import com.openmosque.modules.event.dto.MosqueKhutbahCreateDto;
import com.openmosque.modules.event.dto.MosqueKhutbahResponseDto;
import com.openmosque.modules.event.entity.MosqueKhutbah;
import com.openmosque.modules.event.mapper.EventMapper;
import com.openmosque.modules.event.repository.MosqueKhutbahRepository;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.service.MosqueService;
import com.openmosque.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service managing weekly Friday Jumu'ah prayer schedules and khutbah announcements.
 *
 * WHY THIS IS WRITTEN:
 * Friday prayer (Jumu'ah) is the cornerstone of mosque congregation. Mosques often host
 * multiple shift batches (Batch 1, Batch 2) with rotating guest speakers and live YouTube/Mixlr
 * broadcasts. This service provides structured management and public discovery for Friday schedules.
 *
 * WHERE IT IS USED:
 * - EventPublicController: Public Khutbah schedule (/api/v1/mosques/{idOrSlug}/khutbahs)
 * - MosqueAdminEventController: Imam management portal (/api/v1/mosque-admin/mosques/{id}/khutbahs)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MosqueKhutbahService {

    private final MosqueKhutbahRepository khutbahRepository;
    private final MosqueService mosqueService;
    private final EventMapper eventMapper;
    private final com.openmosque.modules.claim.repository.MosqueClaimRequestRepository claimRequestRepository;

    /**
     * Retrieves upcoming Friday khutbahs for a mosque starting from today.
     *
     * @param idOrSlug Mosque UUID or slug
     * @return List of upcoming Friday Khutbahs ordered by date and batch number
     */
    @Transactional(readOnly = true)
    public List<MosqueKhutbahResponseDto> getUpcomingKhutbahs(String idOrSlug) {
        Mosque mosque = mosqueService.findEntityByIdOrSlug(idOrSlug);
        return eventMapper.toKhutbahDtoList(khutbahRepository.findUpcomingByMosqueId(mosque.getId(), LocalDate.now()));
    }

    /**
     * Retrieves paginated history of all Friday khutbahs for a mosque.
     *
     * @param mosqueId Target mosque UUID
     * @param pageable Pagination parameters
     * @return Paginated list of khutbahs
     */
    @Transactional(readOnly = true)
    public Page<MosqueKhutbahResponseDto> getAllKhutbahs(UUID mosqueId, Pageable pageable) {
        return khutbahRepository.findAllByMosqueId(mosqueId, pageable)
                .map(eventMapper::toDto);
    }

    private void validateMosqueAdminPermission(UUID mosqueId, User user, String action) {
        if (user == null) {
            throw new com.openmosque.common.exception.UnauthorizedException("Authentication required.");
        }
        if (user.getRole() == com.openmosque.modules.user.entity.UserRole.SUPER_ADMIN) {
            return;
        }
        if (user.getRole() == com.openmosque.modules.user.entity.UserRole.MOSQUE_ADMIN) {
            boolean hasApprovedClaim = claimRequestRepository.findByMosqueIdAndClaimantIdAndStatus(
                    mosqueId, user.getId(), com.openmosque.modules.claim.entity.ClaimStatus.APPROVED).isPresent();
            if (hasApprovedClaim) {
                return;
            }
        }
        throw new com.openmosque.common.exception.ForbiddenException(
                "Access denied: You do not have permission to " + action + " for this specific mosque.");
    }

    /**
     * Publishes a new Friday Khutbah announcement.
     * Enforces MOSQUE_ADMIN or SUPER_ADMIN authorization.
     *
     * @param mosqueId Target mosque UUID
     * @param dto Khutbah details (date, topic, khatib, batch time, live stream)
     * @param user Authenticated administrative user
     * @return Published khutbah details
     */
    @Transactional
    public MosqueKhutbahResponseDto createKhutbah(UUID mosqueId, MosqueKhutbahCreateDto dto, User user) {
        validateMosqueAdminPermission(mosqueId, user, "publish Friday Khutbahs");
        Mosque mosque = mosqueService.findEntityByIdOrSlug(mosqueId.toString());

        validateKhutbahTimings(mosque.getId(), dto, null);

        MosqueKhutbah khutbah = eventMapper.toEntity(dto);
        khutbah.setMosque(mosque);

        MosqueKhutbah saved = khutbahRepository.save(khutbah);
        log.info("Created Friday Khutbah '{}' for mosque '{}' by user '{}'", saved.getTopic(), mosque.getName(), user.getEmail());
        return eventMapper.toDto(saved);
    }

    /**
     * Updates an existing Friday Khutbah.
     *
     * @param mosqueId Target mosque UUID
     * @param khutbahId Khutbah UUID to update
     * @param dto Updated details
     * @param user Authenticated administrative user
     * @return Updated khutbah details
     * @throws ForbiddenException if khutbah does not belong to target mosque
     */
    @Transactional
    public MosqueKhutbahResponseDto updateKhutbah(UUID mosqueId, UUID khutbahId, MosqueKhutbahCreateDto dto, User user) {
        validateMosqueAdminPermission(mosqueId, user, "update Friday Khutbahs");
        MosqueKhutbah khutbah = khutbahRepository.findById(khutbahId)
                .filter(k -> !k.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueKhutbah", "id", khutbahId));

        if (!khutbah.getMosque().getId().equals(mosqueId)) {
            throw new ForbiddenException("Khutbah does not belong to the specified mosque.");
        }

        validateKhutbahTimings(mosqueId, dto, khutbahId);

        khutbah.setKhutbahDate(dto.getKhutbahDate());
        khutbah.setTopic(dto.getTopic());
        khutbah.setKhatibName(dto.getKhatibName());
        khutbah.setBatchNumber(dto.getBatchNumber());
        khutbah.setKhutbahTime(dto.getKhutbahTime());
        khutbah.setAdhaanTime(dto.getAdhaanTime());
        khutbah.setIqamahTime(dto.getIqamahTime());
        khutbah.setLanguage(dto.getLanguage());
        khutbah.setStreamUrl(dto.getStreamUrl());
        khutbah.setRecordingUrl(dto.getRecordingUrl());
        khutbah.setNotes(dto.getNotes());

        MosqueKhutbah updated = khutbahRepository.save(khutbah);
        log.info("Updated Friday Khutbah '{}' by user '{}'", khutbahId, user.getEmail());
        return eventMapper.toDto(updated);
    }

    private void validateKhutbahTimings(UUID mosqueId, MosqueKhutbahCreateDto dto, UUID excludeKhutbahId) {
        if (dto.getKhutbahDate() == null || dto.getKhutbahTime() == null) {
            throw new BadRequestException("Khutbah date and time are required.");
        }

        List<MosqueKhutbah> existingList = khutbahRepository.findByMosqueIdAndKhutbahDate(mosqueId, dto.getKhutbahDate());
        for (MosqueKhutbah existing : existingList) {
            if (excludeKhutbahId != null && existing.getId().equals(excludeKhutbahId)) {
                continue;
            }
            if (existing.getBatchNumber() == dto.getBatchNumber()) {
                throw new ConflictException(String.format(
                        "Batch conflict: Batch #%d is already scheduled on %s ('%s'). Please choose a different batch number.",
                        dto.getBatchNumber(), dto.getKhutbahDate(), existing.getTopic()
                ));
            }
            if (existing.getKhutbahTime() != null && existing.getKhutbahTime().equals(dto.getKhutbahTime())) {
                throw new ConflictException(String.format(
                        "Time conflict: Another Khutbah ('%s') is already scheduled at %s on %s. Admin cannot host 2 khutbahs at the exact same time.",
                        existing.getTopic(), dto.getKhutbahTime(), dto.getKhutbahDate()
                ));
            }
        }
    }

    /**
     * Soft-deletes a Friday Khutbah entry.
     *
     * @param mosqueId Target mosque UUID
     * @param khutbahId Khutbah UUID to delete
     * @param user Authenticated administrative user
     */
    @Transactional
    public void deleteKhutbah(UUID mosqueId, UUID khutbahId, User user) {
        validateMosqueAdminPermission(mosqueId, user, "delete Friday Khutbahs");
        MosqueKhutbah khutbah = khutbahRepository.findById(khutbahId)
                .filter(k -> !k.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueKhutbah", "id", khutbahId));

        if (!khutbah.getMosque().getId().equals(mosqueId)) {
            throw new ForbiddenException("Khutbah does not belong to the specified mosque.");
        }

        khutbah.setDeleted(true);
        khutbahRepository.save(khutbah);
        log.info("Soft-deleted Friday Khutbah '{}' by user '{}'", khutbahId, user.getEmail());
    }
}
