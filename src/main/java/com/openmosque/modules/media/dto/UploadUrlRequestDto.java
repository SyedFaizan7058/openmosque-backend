package com.openmosque.modules.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h3>UploadUrlRequestDto</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Transports media metadata (filename, verified MIME content type, target storage folder) from the client
 * to request an authorized, pre-signed upload URL. This architecture prevents media files from choking
 * the application server by enabling direct-to-cloud uploads.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * {@link com.openmosque.modules.media.controller.MediaUploadController#generateUploadUrl}.
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for requesting a pre-signed media upload URL")
public class UploadUrlRequestDto {

    @NotBlank(message = "File name is required")
    @Schema(description = "Original filename", example = "prayer_hall_dome.jpg")
    private String fileName;

    @NotBlank(message = "Content type is required")
    @Pattern(
        regexp = "^(image/jpeg|image/png|image/webp|image/heic|application/pdf)$",
        message = "Content type must be image/jpeg, image/png, image/webp, image/heic, or application/pdf"
    )
    @Schema(description = "MIME Content Type", example = "image/jpeg")
    private String contentType;

    @NotBlank(message = "Folder category is required")
    @Pattern(
        regexp = "^(MOSQUE_IMAGE|PROOF_DOCUMENT|EVENT_BANNER|USER_AVATAR)$",
        message = "Folder category must be MOSQUE_IMAGE, PROOF_DOCUMENT, EVENT_BANNER, or USER_AVATAR"
    )
    @Schema(description = "Storage folder category", example = "MOSQUE_IMAGE")
    private String folderCategory;
}
