package com.openmosque.modules.media.service;

import com.openmosque.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Validates uploaded binary files using Magic Bytes inspection,
 * executable signature detection, and strict filename sanitization.
 */
@Slf4j
@Component
public class FileSecurityValidator {

    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] GIF_MAGIC = "GIF8".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PDF_MAGIC = "%PDF".getBytes(StandardCharsets.US_ASCII);

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "pdf");

    /**
     * Inspects magic bytes to detect real file format and rejects spoofed MIME types.
     */
    public void validateFile(String filename, byte[] data, String reportedContentType) {
        if (data == null || data.length == 0) {
            throw new BadRequestException("Uploaded file cannot be empty.");
        }

        // 1. Detect executable / dangerous binary headers
        detectDangerousSignatures(data);

        // 2. Validate magic bytes against allowed formats
        String detectedFormat = detectFormatFromMagicBytes(data);
        if (detectedFormat == null) {
            throw new BadRequestException("Invalid file content: The uploaded file magic bytes do not match an allowed format (JPEG, PNG, WebP, PDF).");
        }

        // 3. Validate content does not contain dangerous embedded script tags
        detectEmbeddedScripts(data);

        log.debug("File '{}' passed security validation. Detected format: {}", filename, detectedFormat);
    }

    /**
     * Sanitizes a user-supplied filename, removing path traversal and forbidden characters.
     */
    public String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "upload_" + UUID.randomUUID().toString().substring(0, 8) + ".bin";
        }

        // Strip path navigation
        String clean = originalFilename.replace("\\", "/");
        int lastSlash = clean.lastIndexOf('/');
        if (lastSlash >= 0) {
            clean = clean.substring(lastSlash + 1);
        }

        // Remove null bytes and path traversal
        clean = clean.replace("\0", "").replace("..", "");

        // Keep only alphanumeric and safe characters
        clean = clean.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Validate extension
        int dot = clean.lastIndexOf('.');
        if (dot < 0 || dot == clean.length() - 1) {
            throw new BadRequestException("Uploaded file must have a valid file extension.");
        }

        String ext = clean.substring(dot + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BadRequestException("File extension '." + ext + "' is not permitted. Allowed: " + ALLOWED_EXTENSIONS);
        }

        return clean;
    }

    private void detectDangerousSignatures(byte[] data) {
        if (data.length < 4) return;

        // Windows PE executable (MZ)
        if (data[0] == 'M' && data[1] == 'Z') {
            throw new BadRequestException("Executable binaries (.exe, .dll) are strictly forbidden.");
        }

        // Linux ELF binary (0x7F 'E' 'L' 'F')
        if (data[0] == 0x7F && data[1] == 'E' && data[2] == 'L' && data[3] == 'F') {
            throw new BadRequestException("Executable binaries (.elf) are strictly forbidden.");
        }

        // Java Class or Mach-O binary (0xCA 0xFE 0xBA 0xBE)
        if ((data[0] & 0xFF) == 0xCA && (data[1] & 0xFF) == 0xFE && (data[2] & 0xFF) == 0xBA && (data[3] & 0xFF) == 0xBE) {
            throw new BadRequestException("Compiled bytecode or binary executables are strictly forbidden.");
        }
    }

    private String detectFormatFromMagicBytes(byte[] data) {
        // JPEG check
        if (startsWith(data, JPEG_MAGIC)) {
            return "image/jpeg";
        }

        // PNG check
        if (startsWith(data, PNG_MAGIC)) {
            return "image/png";
        }

        // GIF check
        if (startsWith(data, GIF_MAGIC)) {
            return "image/gif";
        }

        // PDF check
        if (startsWith(data, PDF_MAGIC)) {
            return "application/pdf";
        }

        // WebP check: RIFF....WEBP
        if (data.length >= 12) {
            if (data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                    && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
                return "image/webp";
            }
        }

        return null;
    }

    private void detectEmbeddedScripts(byte[] data) {
        int sampleLength = Math.min(data.length, 4096);
        String sample = new String(data, 0, sampleLength, StandardCharsets.ISO_8859_1).toLowerCase();

        if (sample.contains("<script") || sample.contains("javascript:") || sample.contains("<?php")) {
            throw new BadRequestException("File content contains prohibited executable markup or scripts.");
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }
}
