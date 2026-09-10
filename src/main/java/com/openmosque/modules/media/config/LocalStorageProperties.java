package com.openmosque.modules.media.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.storage.local")
public class LocalStorageProperties {

    /**
     * Base directory where uploaded media files are saved on disk.
     */
    private String uploadDir = "./uploads";

    /**
     * Public base URL where static media files are served from.
     */
    private String publicBaseUrl = "http://localhost:8080/api/v1/media/files";

    /**
     * Maximum allowed upload size in Megabytes.
     */
    private long maxFileSizeMb = 15;

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
