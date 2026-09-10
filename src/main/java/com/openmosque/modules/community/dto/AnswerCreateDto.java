package com.openmosque.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to post an answer to a question")
public class AnswerCreateDto {

    @NotBlank(message = "Answer text cannot be empty")
    @Schema(description = "The answer text", example = "Yes, dedicated parking is available right behind the main entrance.")
    private String answerText;
}
