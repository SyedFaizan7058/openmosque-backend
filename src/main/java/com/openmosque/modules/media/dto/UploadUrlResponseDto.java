package com.openmosque.modules.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * <h3>UploadUrlResponseDto</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Returns the short-lived pre-signed PUT upload URL (valid for 15 minutes), the unique storage object key,
 * and the finalized permanent public URL where the media asset will be hosted after upload.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Returned by {@link com.openmosque.modules.media.controller.MediaUploadController#generateUploadUrl} to mobile and web clients.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing pre-signed upload URL and finalized public URL")
public class UploadUrlResponseDto {

    @Schema(description = "Pre-signed URL for direct HTTP PUT upload", example = "https://storage.googleapis.com/bucket/mosques/123.jpg?X-Goog-Algorithm=...")
    private String uploadUrl;

    @Schema(description = "Permanent public URL of the uploaded media file", example = "https://storage.googleapis.com/bucket/mosques/123.jpg")
    private String publicUrl;

    @Schema(description = "Cloud storage object key / path", example = "mosques/prayer_hall_dome_a1b2c3d4.jpg")
    private String objectKey;

    @Schema(description = "Expiration timestamp of the upload URL")
    private Instant expiresAt;
}
