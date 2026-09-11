package com.openmosque.modules.user.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorEnableResponseDto {
    private boolean enabled;
    private List<String> backupCodes;
    private String message;
}
