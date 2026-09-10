package com.openmosque;

import com.openmosque.common.model.ApiResponse;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.user.dto.UserResponseDto;
import com.openmosque.modules.user.dto.UserSyncRequestDto;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.mapper.UserMapper;
import com.openmosque.modules.user.repository.UserRepository;
import com.openmosque.modules.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OpenMosqueApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("Context Loads Successfully")
    void contextLoads() {
        assertThat(userService).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    @DisplayName("Generic ApiResponse structure serializes correctly")
    void testApiResponseEnvelope() {
        ApiResponse<String> successResp = ApiResponse.success("test-payload", "Operation succeeded");
        assertThat(successResp.isSuccess()).isTrue();
        assertThat(successResp.getData()).isEqualTo("test-payload");
        assertThat(successResp.getMessage()).isEqualTo("Operation succeeded");

        ApiResponse<Void> errorResp = ApiResponse.error("INVALID_INPUT", "Error message");
        assertThat(errorResp.isSuccess()).isFalse();
        assertThat(errorResp.getError()).isNotNull();
        assertThat(errorResp.getError().getCode()).isEqualTo("INVALID_INPUT");
    }

    @Test
    @DisplayName("Generic PageResponse wrapper pagination metadata is accurate")
    void testPageResponseWrapper() {
        List<String> items = List.of("Item 1", "Item 2", "Item 3");
        var page = new PageImpl<>(items, PageRequest.of(0, 10), 3);
        PageResponse<String> pageResponse = PageResponse.from(page);

        assertThat(pageResponse.getContent()).hasSize(3);
        assertThat(pageResponse.getTotalElements()).isEqualTo(3);
        assertThat(pageResponse.getPageNumber()).isEqualTo(0);
        assertThat(pageResponse.isFirst()).isTrue();
        assertThat(pageResponse.isLast()).isTrue();
    }

    @Test
    @DisplayName("User synchronization and repository persistence works cleanly")
    void testUserSyncAndMapping() {
        UserSyncRequestDto syncDto = UserSyncRequestDto.builder()
                .firebaseUid("fb-test-uid-" + UUID.randomUUID())
                .email("contributor@openmosque.org")
                .displayName("Brother Ahmad")
                .photoUrl("https://example.com/avatar.jpg")
                .build();

        UserResponseDto createdUser = userService.syncUser(syncDto);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("contributor@openmosque.org");
        assertThat(createdUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(createdUser.getPoints()).isEqualTo(0);

        // Verify entity mapping
        User foundUser = userRepository.findById(createdUser.getId()).orElseThrow();
        assertThat(foundUser.getDisplayName()).isEqualTo("Brother Ahmad");

        // Reward points
        userService.rewardPoints(createdUser.getId(), 50);
        User updatedUser = userRepository.findById(createdUser.getId()).orElseThrow();
        assertThat(updatedUser.getPoints()).isEqualTo(50);
    }
}
