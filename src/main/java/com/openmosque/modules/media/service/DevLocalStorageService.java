package com.openmosque.modules.media.service;

import com.openmosque.common.exception.BadRequestException;
import com.openmosque.modules.media.config.LocalStorageProperties;
import com.openmosque.modules.media.dto.UploadUrlRequestDto;
import com.openmosque.modules.media.dto.UploadUrlResponseDto;
import com.openmosque.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Local development and testing implementation of StorageService.
 * Stores binary files directly on the local filesystem under the configured upload directory
 * and serves them through Spring MVC.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class DevLocalStorageService implements StorageService {

    private final LocalStorageProperties properties;

    @Override
    public UploadUrlResponseDto generatePreSignedUploadUrl(UploadUrlRequestDto request, User user) {
        validateContentType(request.getContentType());

        String cleanFileName = request.getFileName().replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectKey = String.format("%s/%s_%s",
                request.getFolderCategory().toLowerCase(),
                UUID.randomUUID().toString().substring(0, 8),
                cleanFileName);

        Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);
        String uploadUrl = String.format("http://localhost:8080/api/v1/media/upload?key=%s", objectKey);
        String publicUrl = String.format("%s/%s", properties.getPublicBaseUrl(), objectKey);

        log.info("Generated local upload URL for user '{}', file '{}' -> key: '{}'",
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

        Path basePath = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        Path targetPath = basePath.resolve(objectKey).normalize();

        // Prevent path traversal
        if (!targetPath.startsWith(basePath)) {
            throw new BadRequestException("Invalid object key: Path traversal attempt detected");
        }

        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Saved binary file to disk ({} bytes) -> {}", data.length, targetPath);
        } catch (IOException e) {
            log.error("Failed to write media file to disk: {}", e.getMessage(), e);
            throw new BadRequestException("Could not persist file to disk: " + e.getMessage());
        }

        return String.format("%s/%s", properties.getPublicBaseUrl(), objectKey);
    }

    @Override
    public String storeFile(String folderCategory, String originalFileName, byte[] data, String contentType) {
        String cleanFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String objectKey = String.format("%s/%s_%s",
                folderCategory.toLowerCase(),
                UUID.randomUUID().toString().substring(0, 8),
                cleanFileName);
        return storeBinary(objectKey, data, contentType);
    }

    @Override
    public boolean deleteFile(String fileUrlOrKey) {
        String key = fileUrlOrKey;
        if (key.startsWith("http")) {
            int idx = key.indexOf("/files/");
            if (idx != -1) {
                key = key.substring(idx + 7);
            }
        }

        Path basePath = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        Path targetPath = basePath.resolve(key).normalize();

        if (targetPath.startsWith(basePath)) {
            try {
                boolean deleted = Files.deleteIfExists(targetPath);
                log.info("Deleted media file from disk: {} (success: {})", targetPath, deleted);
                return deleted;
            } catch (IOException e) {
                log.warn("Failed to delete media file from disk {}: {}", targetPath, e.getMessage());
                return false;
            }
        }
        return false;
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
