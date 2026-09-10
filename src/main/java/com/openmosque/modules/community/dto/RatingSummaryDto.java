package com.openmosque.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aggregated rating breakdown for a mosque")
public class RatingSummaryDto {

    private long totalReviews;
    private double averageOverall;
    private Double averageCleanliness;
    private Double averageFacilities;
    private Double averageWomensArea;
    private Double averageParking;
}
