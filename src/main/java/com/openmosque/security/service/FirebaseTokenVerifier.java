package com.openmosque.security.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Service responsible for initializing Firebase Admin SDK and verifying Firebase ID tokens.
 * 
 * SECURITY BEST PRACTICES SUPPORTED:
 * 1. External File Path: Loads JSON securely from outside project (e.g. 'file:C:/Users/syedf/.secrets/key.json')
 * 2. Environment Variable JSON: Reads raw JSON string from 'FIREBASE_CREDENTIALS_JSON'
 * 3. Google Default Credentials: Reads from standard 'GOOGLE_APPLICATION_CREDENTIALS' environment variable
 * 4. Dev Mock Mode: Allows local testing with mock Bearer tokens (e.g. 'mock-firebase-uid-001')
 */
@Slf4j
@Service
public class FirebaseTokenVerifier {

    @Value("${app.security.firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Value("${app.security.firebase.service-account-path:}")
    private Resource serviceAccountResource;

    @Value("${FIREBASE_CREDENTIALS_JSON:}")
    private String firebaseCredentialsJson;

    @Value("${app.security.firebase.dev-mock-auth:true}")
    private boolean devMockAuthEnabled;

    private boolean initialized = false;

    /**
     * Initializes the Firebase Admin SDK using secure credential providers.
     */
    @PostConstruct
    public void init() {
        if (!firebaseEnabled) {
            log.info("Firebase Admin SDK disabled by configuration (app.security.firebase.enabled=false). Using dev mock auth.");
            return;
        }

        try {
            GoogleCredentials credentials = resolveCredentials();

            if (credentials != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
                initialized = true;
                log.info("Firebase Admin SDK initialized successfully.");
            } else {
                log.warn("No valid Firebase credentials found. Falling back to dev mock auth.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage());
        }
    }

    /**
     * Resolves Google credentials via file path, environment variable string, or default application credentials.
     */
    private GoogleCredentials resolveCredentials() {
        try {
            // 1. Direct JSON String via environment variable
            if (StringUtils.hasText(firebaseCredentialsJson)) {
                log.info("Loading Firebase credentials from FIREBASE_CREDENTIALS_JSON environment variable.");
                try (InputStream is = new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8))) {
                    return GoogleCredentials.fromStream(is);
                }
            }

            // 2. File Resource (supports 'file:C:/path/to/key.json' or 'classpath:key.json')
            if (serviceAccountResource != null && serviceAccountResource.exists()) {
                log.info("Loading Firebase credentials from configured path: {}", serviceAccountResource.getDescription());
                try (InputStream is = serviceAccountResource.getInputStream()) {
                    return GoogleCredentials.fromStream(is);
                }
            }

            // 3. Fallback to standard GOOGLE_APPLICATION_CREDENTIALS environment variable
            log.info("Attempting to load credentials via Google Application Default Credentials...");
            return GoogleCredentials.getApplicationDefault();

        } catch (Exception e) {
            log.warn("Could not load Google Credentials: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Verifies a Firebase ID token or processes a mock token in development mode.
     * 
     * @param idToken The JWT token string sent in 'Authorization: Bearer <idToken>'
     * @return VerifiedTokenInfo containing user UID and email, or null if invalid.
     */
    public VerifiedTokenInfo verifyToken(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            return null;
        }

        // 1. Explicit mock token support in dev mode (e.g., 'mock-firebase-uid-001')
        if (devMockAuthEnabled && idToken.startsWith("mock-")) {
            log.debug("Dev Mock Auth: Simulating authentication for mock token: {}", idToken);
            String mockUid = idToken;
            String mockEmail = idToken.contains("@") ? idToken : mockUid.replace("mock-", "") + "@openmosque.org";
            return VerifiedTokenInfo.builder()
                    .uid(mockUid)
                    .email(mockEmail)
                    .name("Dev User (" + mockUid + ")")
                    .picture("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde")
                    .build();
        }

        // 2. Real Firebase JWT verification if Firebase SDK is initialized
        if (initialized) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                return VerifiedTokenInfo.builder()
                        .uid(decodedToken.getUid())
                        .email(decodedToken.getEmail())
                        .name(decodedToken.getName())
                        .picture(decodedToken.getPicture())
                        .build();
            } catch (Exception e) {
                log.warn("Firebase ID Token verification failed: {}", e.getMessage());
                // In dev mode, fallback to mock user for arbitrary test strings
                if (devMockAuthEnabled) {
                    log.debug("Dev mode fallback for non-JWT test string: {}", idToken);
                    String mockUid = idToken;
                    return VerifiedTokenInfo.builder()
                            .uid(mockUid)
                            .email(mockUid.contains("@") ? mockUid : mockUid + "@openmosque.org")
                            .name("Dev User (" + mockUid + ")")
                            .picture("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde")
                            .build();
                }
                return null;
            }
        }

        // 3. Fallback when Firebase is disabled in dev mode
        if (devMockAuthEnabled) {
            String mockUid = idToken;
            return VerifiedTokenInfo.builder()
                    .uid(mockUid)
                    .email(mockUid.contains("@") ? mockUid : mockUid + "@openmosque.org")
                    .name("Dev User (" + mockUid + ")")
                    .picture("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde")
                    .build();
        }

        return null;
    }

    /**
     * DTO containing verified claims extracted from the Firebase token.
     */
    @Data
    @Builder
    public static class VerifiedTokenInfo {
        private String uid;
        private String email;
        private String name;
        private String picture;
    }
}
