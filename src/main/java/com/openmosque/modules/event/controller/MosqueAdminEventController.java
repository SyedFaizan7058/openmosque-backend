package com.openmosque.modules.event.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.event.dto.MosqueEventCreateDto;
import com.openmosque.modules.event.dto.MosqueEventResponseDto;
import com.openmosque.modules.event.dto.MosqueKhutbahCreateDto;
import com.openmosque.modules.event.dto.MosqueKhutbahResponseDto;
import com.openmosque.modules.event.service.MosqueEventService;
import com.openmosque.modules.event.service.MosqueKhutbahService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Administrative REST Controller for Mosque Imams and Trustees.
 *
 * WHY THIS IS WRITTEN:
 * Empowers verified mosque administrators to publish upcoming community halaqahs, workshops,
 * and Friday Jumu'ah schedules directly to worshippers, keeping community schedules synchronized.
 *
 * WHERE IT IS USED:
 * - Admin & Mosque Management Portal (/admin and /moderator screens)
 * - Mosque Admin Profile & Event Publishing Dashboard
 */
@RestController
@RequestMapping("/api/v1/mosque-admin/mosques/{id}")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MOSQUE_ADMIN', 'SUPER_ADMIN')")
@Tag(name = "Mosque Admin (Events & Khutbahs)", description = "Management endpoints for Imams and Mosque Admins to schedule events and publish Friday khutbahs")
public class MosqueAdminEventController {

    private final MosqueEventService eventService;
    private final MosqueKhutbahService khutbahService;

    /**
     * Schedules a new mosque community event.
     *
     * @param mosqueId Target mosque UUID
     * @param dto Event payload
     * @param user Authenticated mosque administrator
     * @return Created event details with HTTP 201 Created
     */
    @PostMapping("/events")
    @Operation(summary = "Create mosque event", description = "Schedules a new community program, lecture, halaqah, or workshop.")
    public ResponseEntity<ApiResponse<MosqueEventResponseDto>> createEvent(
            @PathVariable("id") UUID mosqueId,
            @Valid @RequestBody MosqueEventCreateDto dto,
            @CurrentUser User user
    ) {
        MosqueEventResponseDto response = eventService.createEvent(mosqueId, dto, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Event created successfully"));
    }

    /**
     * Modifies an existing mosque community event.
     *
     * @param mosqueId Target mosque UUID
     * @param eventId Event UUID to update
     * @param dto Updated event details
     * @param user Authenticated administrative user
     * @return Updated event details
     */
    @PutMapping("/events/{eventId}")
    @Operation(summary = "Update mosque event", description = "Modifies an existing event's timing, description, speaker, or details.")
    public ResponseEntity<ApiResponse<MosqueEventResponseDto>> updateEvent(
            @PathVariable("id") UUID mosqueId,
            @PathVariable("eventId") UUID eventId,
            @Valid @RequestBody MosqueEventCreateDto dto,
            @CurrentUser User user
    ) {
        MosqueEventResponseDto response = eventService.updateEvent(mosqueId, eventId, dto, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Event updated successfully"));
    }

    /**
     * Soft-deletes or cancels a scheduled mosque event.
     *
     * @param mosqueId Target mosque UUID
     * @param eventId Event UUID to delete
     * @param user Authenticated administrative user
     * @return Success response
     */
    @DeleteMapping("/events/{eventId}")
    @Operation(summary = "Cancel / Delete mosque event", description = "Soft-deletes or cancels a scheduled event.")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable("id") UUID mosqueId,
            @PathVariable("eventId") UUID eventId,
            @CurrentUser User user
    ) {
        eventService.deleteEvent(mosqueId, eventId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Event deleted successfully"));
    }

    /**
     * Publishes a Friday Jumu'ah khutbah announcement with topic, speaker, and live stream.
     *
     * @param mosqueId Target mosque UUID
     * @param dto Khutbah payload
     * @param user Authenticated administrative user
     * @return Created khutbah details with HTTP 201 Created
     */
    @PostMapping("/khutbahs")
    @Operation(summary = "Publish Friday Khutbah", description = "Schedules and publishes upcoming Friday Khutbah topic, speaker, and live stream.")
    public ResponseEntity<ApiResponse<MosqueKhutbahResponseDto>> createKhutbah(
            @PathVariable("id") UUID mosqueId,
            @Valid @RequestBody MosqueKhutbahCreateDto dto,
            @CurrentUser User user
    ) {
        MosqueKhutbahResponseDto response = khutbahService.createKhutbah(mosqueId, dto, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Khutbah published successfully"));
    }

    /**
     * Modifies a published Friday Khutbah.
     *
     * @param mosqueId Target mosque UUID
     * @param khutbahId Khutbah UUID to update
     * @param dto Updated details
     * @param user Authenticated administrative user
     * @return Updated khutbah details
     */
    @PutMapping("/khutbahs/{khutbahId}")
    @Operation(summary = "Update Friday Khutbah", description = "Updates khutbah details, speaker, timings, or recording URL.")
    public ResponseEntity<ApiResponse<MosqueKhutbahResponseDto>> updateKhutbah(
            @PathVariable("id") UUID mosqueId,
            @PathVariable("khutbahId") UUID khutbahId,
            @Valid @RequestBody MosqueKhutbahCreateDto dto,
            @CurrentUser User user
    ) {
        MosqueKhutbahResponseDto response = khutbahService.updateKhutbah(mosqueId, khutbahId, dto, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Khutbah updated successfully"));
    }

    /**
     * Soft-deletes a published Friday Khutbah.
     *
     * @param mosqueId Target mosque UUID
     * @param khutbahId Khutbah UUID to delete
     * @param user Authenticated administrative user
     * @return Success response
     */
    @DeleteMapping("/khutbahs/{khutbahId}")
    @Operation(summary = "Delete Friday Khutbah", description = "Soft-deletes a Friday khutbah entry.")
    public ResponseEntity<ApiResponse<Void>> deleteKhutbah(
            @PathVariable("id") UUID mosqueId,
            @PathVariable("khutbahId") UUID khutbahId,
            @CurrentUser User user
    ) {
        khutbahService.deleteKhutbah(mosqueId, khutbahId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Khutbah deleted successfully"));
    }
}
