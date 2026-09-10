package com.openmosque.modules.event.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.event.dto.MosqueEventResponseDto;
import com.openmosque.modules.event.dto.MosqueKhutbahResponseDto;
import com.openmosque.modules.event.entity.EventType;
import com.openmosque.modules.event.service.MosqueEventService;
import com.openmosque.modules.event.service.MosqueKhutbahService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Public REST Controller for exploring community programs, halaqahs, and Friday Jumu'ah khutbahs.
 *
 * WHY THIS IS WRITTEN:
 * Gives Muslim communities and travellers immediate public visibility into lectures, educational circles,
 * youth programs, and Friday prayer times without requiring user account registration or login.
 *
 * WHERE IT IS USED:
 * - Web Mosque Profile Page (Events & Khutbah tabs in MosqueDetail.jsx)
 * - Community Events Directory (/events in frontend)
 * - Friday Jumu'ah Guide (/jumah in frontend)
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Events & Khutbahs (Public)", description = "Public endpoints for exploring mosque events, lectures, and Friday Jumu'ah khutbah schedules")
public class EventPublicController {

    private final MosqueEventService eventService;
    private final MosqueKhutbahService khutbahService;

    /**
     * Retrieves upcoming scheduled events for a mosque with optional event type filtering.
     *
     * @param idOrSlug Mosque UUID or unique slug
     * @param type Optional filter by category (e.g. HALAQAH, WORKSHOP, YOUTH_PROGRAM)
     * @param pageable Pagination and sorting criteria
     * @return Paginated response containing list of MosqueEventResponseDto
     */
    @GetMapping("/mosques/{idOrSlug}/events")
    @Operation(summary = "List upcoming mosque events", description = "Retrieves paginated upcoming events for a mosque with optional event type filtering.")
    public ResponseEntity<ApiResponse<PageResponse<MosqueEventResponseDto>>> getUpcomingEvents(
            @PathVariable("idOrSlug") String idOrSlug,
            @RequestParam(value = "type", required = false) EventType type,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<MosqueEventResponseDto> page = eventService.getUpcomingEvents(idOrSlug, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page), "Upcoming events retrieved successfully"));
    }

    /**
     * Retrieves full details of a specific community event.
     *
     * @param id Unique event UUID
     * @return Single MosqueEventResponseDto
     */
    @GetMapping("/events/{id}")
    @Operation(summary = "Get event details", description = "Retrieves full details of a specific event.")
    public ResponseEntity<ApiResponse<MosqueEventResponseDto>> getEventById(@PathVariable("id") UUID id) {
        MosqueEventResponseDto event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(event, "Event details retrieved successfully"));
    }

    /**
     * Retrieves scheduled Friday Jumu'ah khutbahs for a mosque.
     *
     * @param idOrSlug Mosque UUID or unique slug
     * @return List of upcoming Friday khutbah topics, times, and live stream URLs
     */
    @GetMapping("/mosques/{idOrSlug}/khutbahs")
    @Operation(summary = "List Friday Jumu'ah khutbahs", description = "Retrieves upcoming scheduled Friday khutbah topics, khatibs, times, and broadcast links.")
    public ResponseEntity<ApiResponse<List<MosqueKhutbahResponseDto>>> getUpcomingKhutbahs(
            @PathVariable("idOrSlug") String idOrSlug
    ) {
        List<MosqueKhutbahResponseDto> khutbahs = khutbahService.getUpcomingKhutbahs(idOrSlug);
        return ResponseEntity.ok(ApiResponse.success(khutbahs, "Khutbahs retrieved successfully"));
    }
}
