package com.openmosque.modules.media.service;

import com.openmosque.common.exception.BadRequestException;
import com.openmosque.modules.media.config.R2StorageProperties;
import com.openmosque.modules.media.dto.UploadUrlRequestDto;
import com.openmosque.modules.media.dto.UploadUrlResponseDto;
import com.openmosque.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Production Cloudflare R2 Object Storage Service (S3-compatible).
 * Enabled when 'app.storage.provider=r2'.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "r2")
public class R2StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final R2StorageProperties properties;
    private final FileSecurityValidator fileSecurityValidator;

    @Override
    public UploadUrlResponseDto generatePreSignedUploadUrl(UploadUrlRequestDto request, User user) {
        validateContentType(request.getContentType());

        String cleanFileName = fileSecurityValidator.sanitizeFilename(request.getFileName());
        String objectKey = String.format("%s/%s_%s",
                request.getFolderCategory().toLowerCase(),
                UUID.randomUUID().toString().substring(0, 8),
                cleanFileName);

        Duration duration = Duration.ofMinutes(properties.getPresignedUrlDurationMinutes());
        Instant expiresAt = Instant.now().plus(duration);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .contentType(request.getContentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(duration)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedPut = s3Presigner.presignPutObject(presignRequest);
        String uploadUrl = presignedPut.url().toString();
        String publicUrl = buildPublicUrl(objectKey);

        log.info("Generated R2 pre-signed upload URL for user '{}', file '{}' -> key: '{}'",
                user.getEmail(), request.getFileName(), objectKey);

        return UploadUrlResponseDto.builder()
                .uploadUrl(uploadUrl)
                .publicUrl(publicUrl)
                .objectKey(objectKey)
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    public String storeBinary(String objectKey, byte[] data, String contentType) {
        validateContentType(contentType);
        validateFileSize(data.length);
        fileSecurityValidator.validateFile(objectKey, data, contentType);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength((long) data.length)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(data));
            log.info("Saved binary file to Cloudflare R2 ({} bytes) -> key: '{}'", data.length, objectKey);
        } catch (Exception e) {
            log.error("Failed to upload binary file to Cloudflare R2: {}", e.getMessage(), e);
            throw new BadRequestException("Could not upload file to Cloudflare R2: " + e.getMessage());
        }

        return buildPublicUrl(objectKey);
    }

    @Override
    public String storeFile(String folderCategory, String originalFileName, byte[] data, String contentType) {
        String cleanFileName = fileSecurityValidator.sanitizeFilename(originalFileName);
        String objectKey = String.format("%s/%s_%s",
                folderCategory.toLowerCase(),
                UUID.randomUUID().toString().substring(0, 8),
                cleanFileName);
        return storeBinary(objectKey, data, contentType);
    }

    @Override
    public boolean deleteFile(String fileUrlOrKey) {
        String key = extractObjectKey(fileUrlOrKey);
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted media file from Cloudflare R2: key='{}'", key);
            return true;
        } catch (Exception e) {
            log.warn("Failed to delete media file from Cloudflare R2 key='{}': {}", key, e.getMessage());
            return false;
        }
    }

    private String buildPublicUrl(String objectKey) {
        String base = properties.getPublicBaseUrl();
        if (base == null || base.isBlank()) {
            base = String.format("%s/%s", properties.getEndpoint(), properties.getBucket());
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return String.format("%s/%s", base, objectKey);
    }

    private String extractObjectKey(String fileUrlOrKey) {
        if (fileUrlOrKey == null) {
            return "";
        }
        String key = fileUrlOrKey.trim();
        String base = properties.getPublicBaseUrl();
        if (base != null && !base.isBlank() && key.startsWith(base)) {
            key = key.substring(base.length());
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
        } else if (key.startsWith("http://") || key.startsWith("https://")) {
            int thirdSlash = key.indexOf('/', 8);
            if (thirdSlash != -1) {
                key = key.substring(thirdSlash + 1);
            }
        }
        return key;
    }

    private void validateContentType(String contentType) {
        if (contentType == null || !properties.getAllowedContentTypes().contains(contentType.toLowerCase())) {
            throw new BadRequestException("Unsupported media type: " + contentType +
                    ". Allowed types: " + properties.getAllowedContentTypes());
        }
    }

    private void validateFileSize(long sizeBytes) {
        long maxBytes = properties.getMaxFileSizeMb() * 1024 * 1024;
        if (sizeBytes > maxBytes) {
            throw new BadRequestException("File size (" + (sizeBytes / 1024 / 1024) +
                    "MB) exceeds maximum limit of " + properties.getMaxFileSizeMb() + "MB");
        }
    }
}
