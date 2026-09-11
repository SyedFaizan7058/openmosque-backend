package com.openmosque.modules.user.controller;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.modules.user.dto.*;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.service.TwoFactorAuthService;
import com.openmosque.security.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/2fa")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Two-Factor Authentication (2FA)", description = "Industry-standard TOTP 2FA endpoints for Google Authenticator, Authy, and Microsoft Authenticator")
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorService;

    @Operation(summary = "Get 2FA status", description = "Checks whether 2FA is active and how many backup codes remain.")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<TwoFactorStatusResponseDto>> getStatus(@CurrentUser User user) {
        TwoFactorStatusResponseDto status = twoFactorService.getStatus(user);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @Operation(summary = "Initiate 2FA setup", description = "Generates a fresh TOTP secret and otpauth:// URI for QR code enrollment.")
    @PostMapping("/setup")
    public ResponseEntity<ApiResponse<TwoFactorSetupResponseDto>> initiateSetup(@CurrentUser User user) {
        TwoFactorSetupResponseDto response = twoFactorService.initiateSetup(user);
        return ResponseEntity.ok(ApiResponse.success(response, "2FA setup initiated. Scan QR code into your authenticator app."));
    }

    @Operation(summary = "Enable 2FA", description = "Confirms 6-digit TOTP code, enables 2FA, and returns 8 one-time backup recovery codes.")
    @PostMapping("/enable")
    public ResponseEntity<ApiResponse<TwoFactorEnableResponseDto>> enableTwoFactor(
            @CurrentUser User user,
            @Valid @RequestBody TwoFactorVerifyRequestDto request
    ) {
        TwoFactorEnableResponseDto response = twoFactorService.enableTwoFactor(user, request.getCode());
        return ResponseEntity.ok(ApiResponse.success(response, "Two-Factor Authentication has been successfully enabled."));
    }

    @Operation(summary = "Verify 2FA code / challenge", description = "Verifies 6-digit TOTP code or one-time backup recovery code.")
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyCode(
            @CurrentUser User user,
            @Valid @RequestBody TwoFactorVerifyRequestDto request
    ) {
        boolean verified = twoFactorService.verifyChallenge(user, request.getCode(), request.getBackupCode());
        return ResponseEntity.ok(ApiResponse.success(verified, "Code verified successfully."));
    }

    @Operation(summary = "Disable 2FA", description = "Disables 2FA with step-up verification (valid code or backup code required).")
    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<Void>> disableTwoFactor(
            @CurrentUser User user,
            @Valid @RequestBody TwoFactorVerifyRequestDto request
    ) {
        twoFactorService.disableTwoFactor(user, request.getCode(), request.getBackupCode());
        return ResponseEntity.ok(ApiResponse.success(null, "Two-Factor Authentication has been disabled."));
    }

    @Operation(summary = "Regenerate backup codes", description = "Generates 8 new backup recovery codes (invalidates all previous codes). Step-up code required.")
    @PostMapping("/regenerate-backup-codes")
    public ResponseEntity<ApiResponse<List<String>>> regenerateBackupCodes(
            @CurrentUser User user,
            @Valid @RequestBody TwoFactorVerifyRequestDto request
    ) {
        List<String> newCodes = twoFactorService.regenerateBackupCodes(user, request.getCode());
        return ResponseEntity.ok(ApiResponse.success(newCodes, "New backup recovery codes generated. Store them securely."));
    }
}
