package com.openmosque.modules.media.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Spring Configuration for Cloudflare R2 / S3 client and pre-signer.
 * Activated only when 'app.storage.provider=r2'.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "r2")
@RequiredArgsConstructor
public class R2Config {

    private final R2StorageProperties properties;

    @Bean
    public S3Client s3Client() {
        log.info("Initializing Cloudflare R2 S3Client for bucket '{}' at endpoint '{}'",
                properties.getBucket(), properties.getEndpoint());

        return S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(properties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        log.info("Initializing Cloudflare R2 S3Presigner for endpoint '{}'", properties.getEndpoint());

        return S3Presigner.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(properties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
