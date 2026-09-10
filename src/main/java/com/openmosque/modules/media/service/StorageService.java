package com.openmosque.modules.media.service;

import com.openmosque.modules.media.dto.UploadUrlRequestDto;
import com.openmosque.modules.media.dto.UploadUrlResponseDto;
import com.openmosque.modules.user.entity.User;

/**
 * Storage Service abstraction for managing cloud media assets.
 *
 * WHY THIS IS WRITTEN:
 * Decouples media storage from cloud vendor lock-in. Allows seamless switching between
 * Google Cloud Storage (GCS) in production and DevLocalStorageService in development
 * without requiring live cloud credentials during testing.
 *
 * WHERE IT IS USED:
 * - MediaUploadController: Generates direct PUT upload URLs for photos (/api/v1/media/upload-url)
 * - MosqueService / MosqueClaimService: Attaching photos and verification documents
 */
public interface StorageService {

    /**
     * Generates an authorized, time-limited pre-signed PUT URL.
     * The mobile or web client uploads the file directly to storage using this URL,
     * avoiding server RAM and network bottlenecks.
     *
     * @param request Request containing original fileName, MIME contentType, and folderCategory
     * @param user Authenticated user requesting the upload
     * @return UploadUrlResponseDto with uploadUrl, publicUrl, objectKey, and expiresAt
     */
    UploadUrlResponseDto generatePreSignedUploadUrl(UploadUrlRequestDto request, User user);

    /**
     * Stores a binary file payload directly under the specified object key.
     *
     * @param objectKey Target relative key/path
     * @param data Binary byte payload
     * @param contentType MIME type of the file
     * @return Publicly accessible URL for the saved media file
     */
    String storeBinary(String objectKey, byte[] data, String contentType);

    /**
     * Convenience method to store a binary file generating an object key automatically.
     *
     * @param folderCategory Category folder (e.g. mosque, avatar, document, event)
     * @param originalFileName Original name of the file
     * @param data Binary byte payload
     * @param contentType MIME type of the file
     * @return Publicly accessible URL for the saved media file
     */
    String storeFile(String folderCategory, String originalFileName, byte[] data, String contentType);

    /**
     * Deletes a file from storage by its public URL or object key.
     *
     * @param fileUrlOrKey Public URL or relative object path
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteFile(String fileUrlOrKey);
}
