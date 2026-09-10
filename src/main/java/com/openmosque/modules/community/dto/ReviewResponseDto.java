package com.openmosque.modules.community.dto;

import com.openmosque.modules.community.entity.ContentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mosque review details")
public class ReviewResponseDto {

    private UUID id;
    private UUID mosqueId;
    private UUID userId;
    private String userDisplayName;
    private String userPhotoUrl;
    private int ratingOverall;
    private Integer ratingCleanliness;
    private Integer ratingFacilities;
    private Integer ratingWomensArea;
    private Integer ratingParking;
    private String reviewText;
    private ContentStatus status;
    private Instant createdAt;
}
