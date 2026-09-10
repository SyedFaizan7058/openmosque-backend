package com.openmosque.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload to submit a mosque review and ratings")
public class ReviewCreateDto {

    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    @Schema(description = "Overall rating (1-5)", example = "5")
    private Integer ratingOverall;

    @Min(1) @Max(5)
    @Schema(description = "Cleanliness rating (1-5)", example = "5")
    private Integer ratingCleanliness;

    @Min(1) @Max(5)
    @Schema(description = "Facilities rating (1-5)", example = "4")
    private Integer ratingFacilities;

    @Min(1) @Max(5)
    @Schema(description = "Women's section rating (1-5)", example = "5")
    private Integer ratingWomensArea;

    @Min(1) @Max(5)
    @Schema(description = "Parking convenience rating (1-5)", example = "4")
    private Integer ratingParking;

    @Schema(description = "Text review of the mosque experience", example = "Spacious prayer hall and very clean wudu areas.")
    private String reviewText;
}
