package com.openmosque;

import com.openmosque.common.exception.BadRequestException;
import com.openmosque.common.exception.UnauthorizedException;
import com.openmosque.modules.user.dto.TwoFactorEnableResponseDto;
import com.openmosque.modules.user.dto.TwoFactorSetupResponseDto;
import com.openmosque.modules.user.dto.TwoFactorStatusResponseDto;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.repository.UserRepository;
import com.openmosque.modules.user.service.TwoFactorAuthService;
import com.openmosque.security.service.TotpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TotpAndTwoFactorSecurityTests {

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private TotpUtil totpUtil;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        String salt = UUID.randomUUID().toString().substring(0, 8);
        testUser = userRepository.save(User.builder()
                .firebaseUid("totp-user-" + salt)
                .email("totp-" + salt + "@openmosque.org")
                .displayName("TOTP User")
                .role(UserRole.USER)
                .active(true)
                .build());
    }

    @Test
    @DisplayName("TotpUtil should generate valid secret, URI, and verify matching TOTP codes")
    void testTotpUtilGenerationAndVerification() {
        String secret = totpUtil.generateSecret();
        assertThat(secret).isNotBlank();
        assertThat(secret.length()).isGreaterThanOrEqualTo(16);

        String uri = totpUtil.generateQrCodeUri("user@openmosque.org", secret, "OpenMosque");
        assertThat(uri).startsWith("otpauth://totp/OpenMosque:user%40openmosque.org");
        assertThat(uri).contains("secret=" + secret);

        // Generate current code
        String code = totpUtil.generateCurrentCode(secret);

        // Verify valid code
        boolean isValid = totpUtil.verifyCode(secret, code);
        assertThat(isValid).isTrue();

        // Verify invalid code
        boolean isInvalid = totpUtil.verifyCode(secret, "000000".equals(code) ? "111111" : "000000");
        assertThat(isInvalid).isFalse();
    }

    @Test
    @DisplayName("2FA lifecycle: InitiateSetup -> Enable -> Verify Challenge -> Disable")
    void testTwoFactorLifecycle() {
        // 1. Initial status is disabled
        TwoFactorStatusResponseDto initialStatus = twoFactorAuthService.getStatus(testUser);
        assertThat(initialStatus.isEnabled()).isFalse();

        // 2. Setup creates secret and qrCodeUri
        TwoFactorSetupResponseDto setupDto = twoFactorAuthService.initiateSetup(testUser);
        assertThat(setupDto.getSecret()).isNotBlank();
        assertThat(setupDto.getQrCodeUri()).contains("otpauth://totp/");

        // Refresh user to get temp secret
        User userInDb = userRepository.findById(testUser.getId()).orElseThrow();
        String tempSecret = userInDb.getTwoFactorTempSecret();
        assertThat(tempSecret).isNotBlank();

        // 3. Generating a valid code enables 2FA and produces 8 backup codes
        String validCode = totpUtil.generateCurrentCode(tempSecret);
        TwoFactorEnableResponseDto enableResponse = twoFactorAuthService.enableTwoFactor(testUser, validCode);

        assertThat(enableResponse.isEnabled()).isTrue();
        assertThat(enableResponse.getBackupCodes()).hasSize(8);

        // Refresh user after enable
        testUser = userRepository.findById(testUser.getId()).orElseThrow();

        // 4. Verify status is enabled
        TwoFactorStatusResponseDto enabledStatus = twoFactorAuthService.getStatus(testUser);
        assertThat(enabledStatus.isEnabled()).isTrue();
        assertThat(enabledStatus.getRemainingBackupCodes()).isEqualTo(8);

        // 5. Verify challenge using TOTP code
        boolean codeVerified = twoFactorAuthService.verifyChallenge(testUser, validCode, null);
        assertThat(codeVerified).isTrue();

        // 6. Verify challenge using a backup code (which consumes it)
        String backupCode = enableResponse.getBackupCodes().get(0);
        boolean backupVerified = twoFactorAuthService.verifyChallenge(testUser, null, backupCode);
        assertThat(backupVerified).isTrue();

        // Refresh user after backup code consumption
        testUser = userRepository.findById(testUser.getId()).orElseThrow();

        // Remaining backup codes count decremented to 7
        TwoFactorStatusResponseDto afterBackupStatus = twoFactorAuthService.getStatus(testUser);
        assertThat(afterBackupStatus.getRemainingBackupCodes()).isEqualTo(7);

        // Re-using the same backup code must throw UnauthorizedException
        assertThatThrownBy(() -> twoFactorAuthService.verifyChallenge(testUser, null, backupCode))
                .isInstanceOf(UnauthorizedException.class);

        // 7. Disabling 2FA with invalid code fails
        assertThatThrownBy(() -> twoFactorAuthService.disableTwoFactor(testUser, "999999", null))
                .isInstanceOf(UnauthorizedException.class);

        // 8. Disabling with another valid TOTP code succeeds
        String disableCode = totpUtil.generateCurrentCode(testUser.getTwoFactorSecret());
        twoFactorAuthService.disableTwoFactor(testUser, disableCode, null);

        testUser = userRepository.findById(testUser.getId()).orElseThrow();
        TwoFactorStatusResponseDto disabledStatus = twoFactorAuthService.getStatus(testUser);
        assertThat(disabledStatus.isEnabled()).isFalse();
    }
}
