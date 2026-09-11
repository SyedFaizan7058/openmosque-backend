package com.openmosque.modules.user.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorSetupResponseDto {
    private String secret;
    private String qrCodeUri;
    private String manualEntryKey;
    private String instructions;
}
