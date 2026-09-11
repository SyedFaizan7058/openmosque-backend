package com.openmosque.modules.user.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorVerifyRequestDto {
    @Size(min = 6, max = 6, message = "TOTP code must be exactly 6 digits")
    private String code;

    @Size(min = 8, max = 12, message = "Backup code must be 8-12 characters")
    private String backupCode;
}
