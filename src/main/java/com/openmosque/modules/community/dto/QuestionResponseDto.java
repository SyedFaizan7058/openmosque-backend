package com.openmosque.modules.community.dto;

import com.openmosque.modules.community.entity.QuestionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Question thread with answers")
public class QuestionResponseDto {

    private UUID id;
    private UUID mosqueId;
    private UUID userId;
    private String userDisplayName;
    private String userPhotoUrl;
    private String questionText;
    private QuestionStatus status;
    private Instant createdAt;

    @Builder.Default
    private List<AnswerResponseDto> answers = new ArrayList<>();
}
