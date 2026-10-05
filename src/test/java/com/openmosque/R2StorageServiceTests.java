package com.openmosque;

import com.openmosque.modules.media.config.R2StorageProperties;
import com.openmosque.modules.media.dto.UploadUrlRequestDto;
import com.openmosque.modules.media.dto.UploadUrlResponseDto;
import com.openmosque.modules.media.service.FileSecurityValidator;
import com.openmosque.modules.media.service.R2StorageService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class R2StorageServiceTests {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private R2StorageProperties properties;
    private FileSecurityValidator fileSecurityValidator;
    private R2StorageService r2StorageService;

    @BeforeEach
    void setUp() {
        properties = new R2StorageProperties();
        properties.setBucket("openmosque-test-bucket");
        properties.setEndpoint("https://test-account.r2.cloudflarestorage.com");
        properties.setPublicBaseUrl("https://media.openmosque.org");
        properties.setAccessKey("test-access-key");
        properties.setSecretKey("test-secret-key");
        properties.setMaxFileSizeMb(15);
        properties.setPresignedUrlDurationMinutes(15);

        fileSecurityValidator = new FileSecurityValidator();
        r2StorageService = new R2StorageService(s3Client, s3Presigner, properties, fileSecurityValidator);
    }

    @Test
    @DisplayName("Should generate valid Cloudflare R2 pre-signed upload URL")
    void testGeneratePreSignedUploadUrl() throws Exception {
        User user = User.builder()
                .email("admin@openmosque.org")
                .role(UserRole.MOSQUE_ADMIN)
                .build();

        UploadUrlRequestDto request = UploadUrlRequestDto.builder()
                .fileName("mosque_hall.jpg")
                .contentType("image/jpeg")
                .folderCategory("MOSQUE_IMAGE")
                .build();

        PresignedPutObjectRequest mockPresigned = mock(PresignedPutObjectRequest.class);
        when(mockPresigned.url()).thenReturn(new URL("https://test-account.r2.cloudflarestorage.com/openmosque-test-bucket/mosque_image/123_mosque_hall.jpg?X-Amz-Signature=test"));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresigned);

        UploadUrlResponseDto response = r2StorageService.generatePreSignedUploadUrl(request, user);

        assertThat(response).isNotNull();
        assertThat(response.getUploadUrl()).contains("r2.cloudflarestorage.com");
        assertThat(response.getPublicUrl()).startsWith("https://media.openmosque.org/mosque_image/");
        assertThat(response.getObjectKey()).contains("mosque_hall.jpg");
        assertThat(response.getExpiresAt()).isNotNull();
        verify(s3Presigner, times(1)).presignPutObject(any(PutObjectPresignRequest.class));
    }

    @Test
    @DisplayName("Should store binary file directly to R2 and return public URL")
    void testStoreBinary() {
        byte[] testData = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String publicUrl = r2StorageService.storeFile("events", "banner.png", testData, "image/png");

        assertThat(publicUrl).startsWith("https://media.openmosque.org/events/");
        assertThat(publicUrl).contains("banner.png");
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Should delete object from R2 by key or URL")
    void testDeleteFile() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        boolean deleted = r2StorageService.deleteFile("https://media.openmosque.org/events/12345_banner.png");

        assertThat(deleted).isTrue();
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }
}
