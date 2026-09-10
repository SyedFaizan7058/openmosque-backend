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
@Schema(description = "Payload to ask a community question about a mosque")
public class QuestionCreateDto {

    @NotBlank(message = "Question text cannot be empty")
    @Schema(description = "Question to ask the community or Imam", example = "Is there separate parking available for sisters?")
    private String questionText;
}
