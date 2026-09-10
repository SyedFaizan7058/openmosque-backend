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
@Schema(description = "Answer to a community question")
public class AnswerResponseDto {

    private UUID id;
    private UUID questionId;
    private UUID userId;
    private String userDisplayName;
    private String userPhotoUrl;
    private String answerText;
    private boolean officialMosqueAdmin;
    private ContentStatus status;
    private Instant createdAt;
}
