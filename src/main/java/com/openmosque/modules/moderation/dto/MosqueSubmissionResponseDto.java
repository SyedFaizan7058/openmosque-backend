package com.openmosque.modules.moderation.dto;

import com.openmosque.modules.moderation.entity.SubmissionStatus;
import com.openmosque.modules.moderation.entity.SubmissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueSubmissionResponseDto {
    private UUID id;
    private UUID submitterId;
    private String submitterName;
    private String submitterEmail;
    private UUID targetMosqueId;
    private SubmissionType submissionType;
    private SubmissionStatus status;
    private String name;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private double latitude;
    private double longitude;
    private String contactPhone;
    private String contactEmail;
    private String websiteUrl;
    private String liveStreamUrl;
    private List<String> facilityCodes;
    private List<String> imageUrls;
    private UUID reviewerId;
    private String reviewerName;
    private String reviewComments;
    private Instant reviewedAt;
    private Instant createdAt;
}
