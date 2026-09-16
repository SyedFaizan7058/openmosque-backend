package com.openmosque.modules.moderation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationCountsDto {
    private long pendingSubmissions;
    private long pendingClaims;
    private long pendingFlags;
}
