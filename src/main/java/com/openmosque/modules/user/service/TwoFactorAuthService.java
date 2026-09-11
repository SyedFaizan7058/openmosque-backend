package com.openmosque.modules.user.service;

import com.openmosque.common.exception.BadRequestException;
import com.openmosque.common.exception.UnauthorizedException;
import com.openmosque.modules.user.dto.*;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.repository.UserRepository;
import com.openmosque.security.service.TotpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {

    private final UserRepository userRepository;
    private final TotpUtil totpUtil;
    private static final String ISSUER = "OpenMosque";

    @Transactional
    public TwoFactorSetupResponseDto initiateSetup(User user) {
        if (user.isTwoFactorEnabled()) {
            throw new BadRequestException("Two-Factor Authentication is already enabled for your account.");
        }

        String secret = totpUtil.generateSecret();
        user.setTwoFactorTempSecret(secret);
        userRepository.save(user);

        String qrUri = totpUtil.generateQrCodeUri(user.getEmail(), secret, ISSUER);

        log.info("Initiated 2FA enrollment for user '{}'", user.getEmail());

        return TwoFactorSetupResponseDto.builder()
                .secret(secret)
                .qrCodeUri(qrUri)
                .manualEntryKey(secret)
                .instructions("Scan this QR code in Google Authenticator, Microsoft Authenticator, or Authy, then confirm with the 6-digit code.")
                .build();
    }

    @Transactional
    public TwoFactorEnableResponseDto enableTwoFactor(User user, String code) {
        if (user.isTwoFactorEnabled()) {
            throw new BadRequestException("Two-Factor Authentication is already enabled.");
        }

        if (!StringUtils.hasText(user.getTwoFactorTempSecret())) {
            throw new BadRequestException("No pending 2FA enrollment found. Please initiate setup first.");
        }

        boolean valid = totpUtil.verifyCode(user.getTwoFactorTempSecret(), code);
        if (!valid) {
            log.warn("Invalid TOTP verification code during 2FA enrollment for user '{}'", user.getEmail());
            throw new BadRequestException("Invalid 6-digit verification code. Please check your authenticator app and ensure your device clock is synchronized.");
        }

        // Commit the secret
        user.setTwoFactorSecret(user.getTwoFactorTempSecret());
        user.setTwoFactorTempSecret(null);
        user.setTwoFactorEnabled(true);

        // Generate 8 backup recovery codes
        List<String> rawBackupCodes = totpUtil.generateBackupCodes(8);
        List<String> hashedBackupCodes = rawBackupCodes.stream()
                .map(totpUtil::hashBackupCode)
                .collect(Collectors.toList());

        user.setTwoFactorBackupCodes(String.join(",", hashedBackupCodes));
        userRepository.save(user);

        log.info("Successfully enabled 2FA for user '{}'", user.getEmail());

        return TwoFactorEnableResponseDto.builder()
                .enabled(true)
                .backupCodes(rawBackupCodes)
                .message("Two-Factor Authentication is now enabled. Save your recovery backup codes in a secure place. They will not be displayed again.")
                .build();
    }

    @Transactional
    public boolean verifyChallenge(User user, String code, String backupCode) {
        if (!user.isTwoFactorEnabled()) {
            return true;
        }

        // 1. Try TOTP 6-digit code
        if (StringUtils.hasText(code)) {
            if (totpUtil.verifyCode(user.getTwoFactorSecret(), code)) {
                log.info("2FA TOTP code verified successfully for user '{}'", user.getEmail());
                return true;
            }
        }

        // 2. Try one-time backup recovery code
        if (StringUtils.hasText(backupCode)) {
            List<String> hashedCodes = parseBackupCodes(user.getTwoFactorBackupCodes());
            Optional<String> matchedHash = totpUtil.findAndConsumeBackupCode(backupCode, hashedCodes);
            if (matchedHash.isPresent()) {
                hashedCodes.remove(matchedHash.get());
                user.setTwoFactorBackupCodes(String.join(",", hashedCodes));
                userRepository.save(user);
                log.info("2FA backup recovery code used and consumed for user '{}' ({} codes remaining)",
                        user.getEmail(), hashedCodes.size());
                return true;
            }
        }

        log.warn("Failed 2FA verification attempt for user '{}'", user.getEmail());
        throw new UnauthorizedException("Invalid Two-Factor Authentication code or backup recovery code.");
    }

    @Transactional
    public void disableTwoFactor(User user, String code, String backupCode) {
        if (!user.isTwoFactorEnabled()) {
            throw new BadRequestException("Two-Factor Authentication is not enabled.");
        }

        // Step-up authentication: Must supply valid TOTP code or backup code to disable 2FA
        verifyChallenge(user, code, backupCode);

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setTwoFactorTempSecret(null);
        user.setTwoFactorBackupCodes(null);
        userRepository.save(user);

        log.info("Disabled Two-Factor Authentication for user '{}'", user.getEmail());
    }

    @Transactional
    public List<String> regenerateBackupCodes(User user, String code) {
        if (!user.isTwoFactorEnabled()) {
            throw new BadRequestException("Two-Factor Authentication is not enabled.");
        }

        // Step-up verification: require valid current TOTP code
        if (!totpUtil.verifyCode(user.getTwoFactorSecret(), code)) {
            throw new UnauthorizedException("Invalid TOTP code. A valid 6-digit code is required to regenerate backup codes.");
        }

        List<String> rawCodes = totpUtil.generateBackupCodes(8);
        List<String> hashedCodes = rawCodes.stream()
                .map(totpUtil::hashBackupCode)
                .collect(Collectors.toList());

        user.setTwoFactorBackupCodes(String.join(",", hashedCodes));
        userRepository.save(user);

        log.info("Regenerated 8 new 2FA backup codes for user '{}'", user.getEmail());
        return rawCodes;
    }

    @Transactional(readOnly = true)
    public TwoFactorStatusResponseDto getStatus(User user) {
        int remaining = 0;
        if (user.isTwoFactorEnabled() && StringUtils.hasText(user.getTwoFactorBackupCodes())) {
            remaining = parseBackupCodes(user.getTwoFactorBackupCodes()).size();
        }
        return TwoFactorStatusResponseDto.builder()
                .enabled(user.isTwoFactorEnabled())
                .remainingBackupCodes(remaining)
                .build();
    }

    private List<String> parseBackupCodes(String codesStr) {
        if (!StringUtils.hasText(codesStr)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(codesStr.split(",")));
    }
}
