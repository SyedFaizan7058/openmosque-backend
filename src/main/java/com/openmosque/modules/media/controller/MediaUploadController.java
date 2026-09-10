package com.openmosque.modules.media.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.media.dto.UploadUrlRequestDto;
import com.openmosque.modules.media.dto.UploadUrlResponseDto;
import com.openmosque.modules.media.service.StorageService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller providing secure, direct-to-cloud pre-signed upload URLs.
 *
 * WHY THIS IS WRITTEN:
 * High-resolution mosque photos and committee verification documents represent heavy binary loads.
 * Uploading them directly from web/mobile clients to cloud storage (GCS/Firebase) eliminates
 * server network throttling, saves server memory, and speeds up user upload completion.
 *
 * WHERE IT IS USED:
 * - Mosque Crowdsource Wizard: Uploading cover and interior mosque photos
 * - Mosque Claim Portal: Uploading official charity / trustee certificates
 * - Events Management: Uploading event banner posters
 * - User Profile: Uploading profile avatar photos
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Tag(name = "Media Uploads (Cloud Storage)", description = "Endpoints for pre-signed direct upload URLs to Google Cloud Storage")
public class MediaUploadController {

    private final StorageService storageService;

    /**
     * Issues an authenticated pre-signed PUT URL.
     * Enforces strict content type validation (JPEG, PNG, WebP, HEIC, PDF) and generates unique keys.
     *
     * @param request Payload containing fileName, contentType, and folderCategory
     * @param user Authenticated user making the upload request
     * @return UploadUrlResponseDto containing uploadUrl and finalized publicUrl
     */
    @PostMapping("/upload-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate pre-signed upload URL", description = "Returns a direct-to-cloud PUT URL expiring in 15 minutes. Supports MOSQUE_IMAGE, PROOF_DOCUMENT, EVENT_BANNER, USER_AVATAR.")
    public ResponseEntity<ApiResponse<UploadUrlResponseDto>> getUploadUrl(
            @Valid @RequestBody UploadUrlRequestDto request,
            @CurrentUser User user
    ) {
        UploadUrlResponseDto response = storageService.generatePreSignedUploadUrl(request, user);
        return ResponseEntity.ok(ApiResponse.success(response, "Upload URL generated successfully"));
    }

    /**
     * Binary PUT upload endpoint for direct client uploads via pre-signed URL.
     *
     * @param key Target object key
     * @param data Raw binary bytes
     * @param contentType Content type of the uploaded file
     * @return Success response containing the public URL
     */
    @PutMapping(value = {"/upload", "/mock-upload"})
    @Operation(summary = "Direct binary upload sink", description = "Receives binary byte stream and persists it directly to storage provider.")
    public ResponseEntity<ApiResponse<String>> uploadBinary(
            @RequestParam("key") String key,
            @RequestBody byte[] data,
            @RequestHeader(value = "Content-Type", defaultValue = "application/octet-stream") String contentType
    ) {
        String publicUrl = storageService.storeBinary(key, data, contentType);
        return ResponseEntity.ok(ApiResponse.success(publicUrl, "File uploaded successfully"));
    }

    /**
     * Direct multipart upload endpoint for form-based file uploads.
     *
     * @param file Multipart file
     * @param category Target folder category
     * @param user Authenticated user
     * @return Upload result containing the public URL
     */
    @PostMapping(value = "/upload-direct", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Direct multipart file upload", description = "Uploads a file directly using multipart form data.")
    public ResponseEntity<ApiResponse<String>> uploadDirect(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "category", defaultValue = "mosque") String category,
            @CurrentUser User user
    ) throws java.io.IOException {
        String publicUrl = storageService.storeFile(
                category,
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.bin",
                file.getBytes(),
                file.getContentType()
        );
        return ResponseEntity.ok(ApiResponse.success(publicUrl, "File uploaded successfully"));
    }
}
