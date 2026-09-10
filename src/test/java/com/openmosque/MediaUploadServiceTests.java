package com.openmosque;

import com.openmosque.modules.media.dto.UploadUrlRequestDto;
import com.openmosque.modules.media.dto.UploadUrlResponseDto;
import com.openmosque.modules.media.service.StorageService;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MediaUploadServiceTests {

    @Autowired
    private StorageService storageService;

    @Test
    @DisplayName("Should generate valid pre-signed upload URL for mosque image")
    void testGenerateUploadUrl() {
        User user = User.builder()
                .email("contributor@openmosque.org")
                .role(UserRole.USER)
                .build();

        UploadUrlRequestDto request = UploadUrlRequestDto.builder()
                .fileName("front_dome.jpg")
                .contentType("image/jpeg")
                .folderCategory("MOSQUE_IMAGE")
                .build();

        UploadUrlResponseDto response = storageService.generatePreSignedUploadUrl(request, user);

        assertThat(response).isNotNull();
        assertThat(response.getUploadUrl()).isNotBlank();
        assertThat(response.getPublicUrl()).contains("mosque_image");
        assertThat(response.getObjectKey()).contains("front_dome.jpg");
        assertThat(response.getExpiresAt()).isNotNull();
    }
}
