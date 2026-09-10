package com.openmosque.modules.event.service;

import com.openmosque.common.exception.BadRequestException;
import com.openmosque.common.exception.ConflictException;
import com.openmosque.common.exception.ForbiddenException;
import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.common.util.SecurityUtils;
import com.openmosque.modules.event.dto.MosqueEventCreateDto;
import com.openmosque.modules.event.dto.MosqueEventResponseDto;
import com.openmosque.modules.event.entity.EventType;
import com.openmosque.modules.event.entity.MosqueEvent;
import com.openmosque.modules.event.mapper.EventMapper;
import com.openmosque.modules.event.repository.MosqueEventRepository;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.service.MosqueService;
import com.openmosque.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Service managing community events and programs for mosques.
 *
 * WHY THIS IS WRITTEN:
 * Empowers mosques to broadcast real-time events (Tafseer halaqahs, youth programs,
 * Ramadan Taraweeh schedules, Eid prayers, and workshops) to their local worshippers.
 *
 * WHERE IT IS USED:
 * - EventPublicController: Public event explorer (/api/v1/mosques/{idOrSlug}/events)
 * - MosqueAdminEventController: Imam & Committee portal (/api/v1/mosque-admin/mosques/{id}/events)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MosqueEventService {

    private final MosqueEventRepository eventRepository;
    private final MosqueService mosqueService;
    private final EventMapper eventMapper;
    private final com.openmosque.modules.claim.repository.MosqueClaimRequestRepository claimRequestRepository;

    /**
     * Retrieves paginated upcoming events for a mosque with optional event type filtering.
     *
     * @param idOrSlug Mosque UUID or slug
     * @param eventType Optional category filter (HALAQAH, WORKSHOP, YOUTH_PROGRAM, etc.)
     * @param pageable Pagination and sort parameters
     * @return Paginated list of active upcoming events
     */
    @Transactional(readOnly = true)
    public Page<MosqueEventResponseDto> getUpcomingEvents(String idOrSlug, EventType eventType, Pageable pageable) {
        Mosque mosque = mosqueService.findEntityByIdOrSlug(idOrSlug);
        return eventRepository.findUpcomingEventsByMosqueIdFiltered(mosque.getId(), eventType, Instant.now(), pageable)
                .map(eventMapper::toDto);
    }

    /**
     * Retrieves full details of a specific event.
     *
     * @param eventId UUID of the event
     * @return Event details DTO
     * @throws ResourceNotFoundException if event is not found or soft-deleted
     */
    @Transactional(readOnly = true)
    public MosqueEventResponseDto getEventById(UUID eventId) {
        MosqueEvent event = eventRepository.findById(eventId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueEvent", "id", eventId));
        return eventMapper.toDto(event);
    }

    /**
     * Creates and schedules a new mosque event.
     * Enforces administrative authorization (MOSQUE_ADMIN or SUPER_ADMIN).
     *
     * @param mosqueId Target mosque UUID
     * @param dto Creation payload containing title, times, speaker, and audience
     * @param user Authenticated user creating the event
     * @return Created event details
     */
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

    @Transactional
    public MosqueEventResponseDto createEvent(UUID mosqueId, MosqueEventCreateDto dto, User user) {
        validateMosqueAdminPermission(mosqueId, user, "schedule mosque events");
        Mosque mosque = mosqueService.findEntityByIdOrSlug(mosqueId.toString());

        validateEventTimings(mosque.getId(), dto.getStartDateTime(), dto.getEndDateTime(), null);

        MosqueEvent event = eventMapper.toEntity(dto);
        event.setMosque(mosque);
        event.setCreatedBy(user);

        MosqueEvent saved = eventRepository.save(event);
        log.info("Created mosque event '{}' for mosque '{}' by user '{}'", saved.getTitle(), mosque.getName(), user.getEmail());
        return eventMapper.toDto(saved);
    }

    /**
     * Updates an existing event's details, timings, or speakers.
     *
     * @param mosqueId Target mosque UUID
     * @param eventId Event UUID to update
     * @param dto Updated event details
     * @param user Authenticated administrative user
     * @return Updated event details
     * @throws ForbiddenException if event does not belong to the target mosque
     */
    @Transactional
    public MosqueEventResponseDto updateEvent(UUID mosqueId, UUID eventId, MosqueEventCreateDto dto, User user) {
        validateMosqueAdminPermission(mosqueId, user, "update mosque events");
        MosqueEvent event = eventRepository.findById(eventId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueEvent", "id", eventId));

        if (!event.getMosque().getId().equals(mosqueId)) {
            throw new ForbiddenException("Event does not belong to the specified mosque.");
        }

        validateEventTimings(mosqueId, dto.getStartDateTime(), dto.getEndDateTime(), eventId);

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setEventType(dto.getEventType());
        event.setAudience(dto.getAudience());
        event.setStartDateTime(dto.getStartDateTime());
        event.setEndDateTime(dto.getEndDateTime());
        event.setLocationDetails(dto.getLocationDetails());
        event.setSpeakerName(dto.getSpeakerName());
        event.setBannerImageUrl(dto.getBannerImageUrl());
        event.setRegistrationUrl(dto.getRegistrationUrl());

        MosqueEvent updated = eventRepository.save(event);
        log.info("Updated mosque event '{}' by user '{}'", eventId, user.getEmail());
        return eventMapper.toDto(updated);
    }

    private void validateEventTimings(UUID mosqueId, Instant startDateTime, Instant endDateTime, UUID excludeEventId) {
        if (startDateTime == null || endDateTime == null) {
            throw new BadRequestException("Event start and end times are required.");
        }
        if (!endDateTime.isAfter(startDateTime)) {
            throw new BadRequestException("Event end time must be strictly after the start time.");
        }

        List<MosqueEvent> conflicts = eventRepository.findConflictingEvents(
                mosqueId,
                startDateTime,
                endDateTime,
                excludeEventId
        );

        if (!conflicts.isEmpty()) {
            MosqueEvent existing = conflicts.get(0);
            throw new ConflictException(String.format(
                    "Event timing conflicts with existing event '%s' (%s to %s)",
                    existing.getTitle(), existing.getStartDateTime(), existing.getEndDateTime()
            ));
        }
    }

    /**
     * Soft-deletes / cancels a scheduled event.
     *
     * @param mosqueId Target mosque UUID
     * @param eventId Event UUID to delete
     * @param user Authenticated administrative user
     */
    @Transactional
    public void deleteEvent(UUID mosqueId, UUID eventId, User user) {
        validateMosqueAdminPermission(mosqueId, user, "delete mosque events");
        MosqueEvent event = eventRepository.findById(eventId)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("MosqueEvent", "id", eventId));

        if (!event.getMosque().getId().equals(mosqueId)) {
            throw new ForbiddenException("Event does not belong to the specified mosque.");
        }

        event.setDeleted(true);
        eventRepository.save(event);
        log.info("Soft-deleted mosque event '{}' by user '{}'", eventId, user.getEmail());
    }
}
