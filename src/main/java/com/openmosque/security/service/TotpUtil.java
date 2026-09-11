package com.openmosque.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

/**
 * Standard RFC 6238 / RFC 4226 Time-based One-Time Password (TOTP) Utility.
 * Fully compatible with Google Authenticator, Microsoft Authenticator, Authy, etc.
 */
@Slf4j
@Component
public class TotpUtil {

    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final String HMAC_ALGO = "HmacSHA1";
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a secure, random 160-bit (20-byte) Base32 secret key.
     */
    public String generateSecret() {
        byte[] buffer = new byte[20];
        SECURE_RANDOM.nextBytes(buffer);
        return encodeBase32(buffer);
    }

    /**
     * Builds the standard otpauth:// URI for authenticator QR enrollment.
     */
    public String generateQrCodeUri(String email, String secret, String issuer) {
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8).replace("+", "%20");
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8).replace("+", "%20");
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                encodedIssuer, encodedEmail, secret, encodedIssuer);
    }

    /**
     * Validates a 6-digit TOTP code against the secret key with +/- 1 time step tolerance (30 seconds drift).
     */
    public boolean verifyCode(String secret, String codeStr) {
        if (secret == null || codeStr == null) {
            return false;
        }

        String cleanCode = codeStr.trim();
        if (cleanCode.length() != CODE_DIGITS) {
            return false;
        }

        long code;
        try {
            code = Long.parseLong(cleanCode);
        } catch (NumberFormatException e) {
            return false;
        }

        long currentWindow = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;

        // Check window -1, 0, +1 for clock drift tolerance
        for (int i = -1; i <= 1; i++) {
            if (generateCodeForWindow(secret, currentWindow + i) == code) {
                return true;
            }
        }
        return false;
    }

    private long generateCodeForWindow(String secret, long window) {
        byte[] key = decodeBase32(secret);
        if (key.length == 0) return -1;

        byte[] data = ByteBuffer.allocate(8).putLong(window).array();
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(key, HMAC_ALGO));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xF;
            long truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= (long) Math.pow(10, CODE_DIGITS);
            return truncatedHash;
        } catch (Exception e) {
            log.error("Error calculating TOTP hash: {}", e.getMessage());
            return -1;
        }
    }

    public String generateCurrentCode(String secret) {
        long currentWindow = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        long code = generateCodeForWindow(secret, currentWindow);
        return String.format("%06d", code);
    }

    /**
     * Generates a set of 8 random alphanumeric backup/recovery codes.
     */
    public List<String> generateBackupCodes(int count) {
        List<String> codes = new ArrayList<>();
        char[] chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
        for (int i = 0; i < count; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 8; j++) {
                if (j == 4) sb.append('-');
                sb.append(chars[SECURE_RANDOM.nextInt(chars.length)]);
            }
            codes.add(sb.toString());
        }
        return codes;
    }

    /**
     * Hashes a backup code with SHA-256 for secure database storage.
     */
    public String hashBackupCode(String rawCode) {
        try {
            String normalized = rawCode.replace("-", "").trim().toUpperCase();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing backup code", e);
        }
    }

    /**
     * Verifies if a raw code matches any hashed backup code.
     * If matched, returns the matched hash so it can be consumed and removed.
     */
    public Optional<String> findAndConsumeBackupCode(String rawCode, List<String> hashedCodes) {
        if (rawCode == null || hashedCodes == null || hashedCodes.isEmpty()) {
            return Optional.empty();
        }
        String candidateHash = hashBackupCode(rawCode);
        return hashedCodes.stream().filter(h -> h.equals(candidateHash)).findFirst();
    }

    // --- Base32 Encoding / Decoding Helpers ---

    private String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder();
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                result.append(BASE32_CHARS.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }
        if (bitsLeft > 0) {
            buffer = buffer << (5 - bitsLeft);
            result.append(BASE32_CHARS.charAt(buffer & 0x1F));
        }
        return result.toString();
    }

    private byte[] decodeBase32(String base32) {
        String clean = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");
        int outputBytes = clean.length() * 5 / 8;
        byte[] result = new byte[outputBytes];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;
        for (char c : clean.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val == -1) continue;
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                if (index < result.length) {
                    result[index++] = (byte) ((buffer >> bitsLeft) & 0xFF);
                }
            }
        }
        return result;
    }
}
