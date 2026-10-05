package com.openmosque.modules.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties for Cloudflare R2 Object Storage (S3 compatible).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.storage.r2")
public class R2StorageProperties {

    /**
     * Cloudflare R2 Access Key ID.
     */
    private String accessKey;

    /**
     * Cloudflare R2 Secret Access Key.
     */
    private String secretKey;

    /**
     * Cloudflare R2 S3 API Endpoint, e.g. https://<account_id>.r2.cloudflarestorage.com
     */
    private String endpoint;

    /**
     * Cloudflare R2 Bucket name, e.g. openmosque-media
     */
    private String bucket;

    /**
     * Public base URL where static media files are served from,
     * e.g. https://media.openmosque.org or https://pub-<hash>.r2.dev
     */
    private String publicBaseUrl;

    /**
     * Maximum allowed upload size in Megabytes.
     */
    private long maxFileSizeMb = 15;

    /**
     * Pre-signed PUT URL expiration in minutes.
     */
    private int presignedUrlDurationMinutes = 15;

    /**
     * Supported MIME types for uploaded files.
     */
    private List<String> allowedContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/heic",
            "application/pdf"
    );
}
