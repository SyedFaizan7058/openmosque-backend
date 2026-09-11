package com.openmosque;

import com.openmosque.common.exception.BadRequestException;
import com.openmosque.modules.media.service.FileSecurityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileSecurityAndUploadTests {

    private FileSecurityValidator fileSecurityValidator;

    @BeforeEach
    void setUp() {
        fileSecurityValidator = new FileSecurityValidator();
    }

    @Test
    @DisplayName("FileSecurityValidator should approve valid JPEG, PNG, WebP, and PDF magic bytes")
    void testValidFileMagicBytes() {
        // Valid JPEG
        byte[] jpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10};
        fileSecurityValidator.validateFile("photo.jpg", jpeg, "image/jpeg");

        // Valid PNG
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00};
        fileSecurityValidator.validateFile("graphic.png", png, "image/png");

        // Valid WebP (RIFF....WEBP)
        byte[] webp = new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 20, 'W', 'E', 'B', 'P'};
        fileSecurityValidator.validateFile("banner.webp", webp, "image/webp");

        // Valid PDF (%PDF-)
        byte[] pdf = new byte[]{'%', 'P', 'D', 'F', '-', '1', '.', '7'};
        fileSecurityValidator.validateFile("document.pdf", pdf, "application/pdf");
    }

    @Test
    @DisplayName("FileSecurityValidator should reject spoofed files with invalid magic bytes")
    void testSpoofedFileRejection() {
        byte[] fakeData = "This is not a real image file".getBytes();
        assertThatThrownBy(() -> fileSecurityValidator.validateFile("fake.png", fakeData, "image/png"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("magic bytes do not match an allowed format");
    }

    @Test
    @DisplayName("FileSecurityValidator should reject executable headers and script content")
    void testExecutableAndScriptRejection() {
        // Windows PE executable (MZ)
        byte[] exe = new byte[]{'M', 'Z', (byte) 0x90, 0x00};
        assertThatThrownBy(() -> fileSecurityValidator.validateFile("malware.jpg", exe, "image/jpeg"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Executable binaries (.exe, .dll) are strictly forbidden.");

        // Linux ELF binary
        byte[] elf = new byte[]{0x7F, 'E', 'L', 'F', 0x01};
        assertThatThrownBy(() -> fileSecurityValidator.validateFile("payload.pdf", elf, "application/pdf"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Executable binaries (.elf) are strictly forbidden.");

        // HTML/XSS script tag in upload
        byte[] scriptInPng = "<script>alert('xss')</script>".getBytes();
        assertThatThrownBy(() -> fileSecurityValidator.validateFile("xss.png", scriptInPng, "image/png"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("FileSecurityValidator should sanitize path traversal sequences")
    void testPathTraversalSanitization() {
        String sanitized1 = fileSecurityValidator.sanitizeFilename("../../../etc/passwd.png");
        assertThat(sanitized1).doesNotContain("..");
        assertThat(sanitized1).doesNotContain("/");
        assertThat(sanitized1).endsWith(".png");

        String sanitized2 = fileSecurityValidator.sanitizeFilename("..\\..\\boot.jpg");
        assertThat(sanitized2).doesNotContain("..");
        assertThat(sanitized2).doesNotContain("\\");
        assertThat(sanitized2).endsWith(".jpg");

        String sanitized3 = fileSecurityValidator.sanitizeFilename("normal_file.png");
        assertThat(sanitized3).isEqualTo("normal_file.png");
    }
}
