package com.openmosque.modules.moderation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformStatsDto {
    private long totalMosques;
    private long verifiedMosques;
    private long pendingSubmissions;
    private long pendingClaims;
    private long activeFlags;
    private long totalUsers;
}
