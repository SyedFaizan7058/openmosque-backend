package com.openmosque.modules.user.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorStatusResponseDto {
    private boolean enabled;
    private int remainingBackupCodes;
}
