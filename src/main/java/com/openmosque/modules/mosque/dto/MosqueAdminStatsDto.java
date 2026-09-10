package com.openmosque.modules.mosque.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueAdminStatsDto {
    private long totalFavorites;
    private long totalReviews;
    private double averageRating;
    private Map<Integer, Long> ratingsBreakdown;
    private long upcomingEventsCount;
    private long unansweredQuestionsCount;
}
