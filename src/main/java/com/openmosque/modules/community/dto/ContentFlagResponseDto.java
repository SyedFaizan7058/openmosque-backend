package com.openmosque.modules.community.dto;

import com.openmosque.modules.community.entity.FlagStatus;
import com.openmosque.modules.community.entity.TargetType;
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
@Schema(description = "Details of a content moderation flag")
public class ContentFlagResponseDto {

    private UUID id;
    private TargetType targetType;
    private UUID targetId;
    private UUID reporterId;
    private String reporterEmail;
    private String reason;
    private FlagStatus status;
    private UUID reviewerId;
    private String reviewerNotes;
    private Instant createdAt;
}
