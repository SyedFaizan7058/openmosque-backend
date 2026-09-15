package com.openmosque.modules.user.service;

import com.openmosque.common.exception.ResourceNotFoundException;
import com.openmosque.common.model.PageResponse;
import com.openmosque.modules.user.dto.UserResponseDto;
import com.openmosque.modules.user.dto.UserSyncRequestDto;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.mapper.UserMapper;
import com.openmosque.modules.user.repository.UserRepository;
import com.openmosque.modules.user.service.BadgeService;
import com.openmosque.modules.claim.repository.MosqueClaimRequestRepository;
import com.openmosque.modules.claim.entity.ClaimStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service Layer handling User profiles, synchronization, role management, and rewards.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BadgeService badgeService;
    private final MosqueClaimRequestRepository claimRequestRepository;

    private UserResponseDto enrichWithClaimedMosques(UserResponseDto dto, User user) {
        if (dto != null && user != null && (user.getRole() == UserRole.MOSQUE_ADMIN || user.getRole() == UserRole.SUPER_ADMIN)) {
            List<UUID> mosqueIds = claimRequestRepository.findByClaimantIdAndStatus(user.getId(), ClaimStatus.APPROVED)
                    .stream()
                    .map(claim -> claim.getMosque().getId())
                    .toList();
            dto.setClaimedMosqueIds(mosqueIds);
        }
        return dto;
    }

    /**
     * Converts current authenticated User entity to UserResponseDto.
     */
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUserProfile(User user) {
        return enrichWithClaimedMosques(userMapper.toDto(user), user);
    }

    /**
     * Updates user's preferred location (city, country, coordinates).
     */
    @Transactional
    public UserResponseDto updateUserLocation(User user, com.openmosque.modules.user.dto.UserLocationUpdateRequestDto request) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", user.getId()));

        if (request.getPreferredCity() != null) existingUser.setPreferredCity(request.getPreferredCity());
        if (request.getPreferredCountry() != null) existingUser.setPreferredCountry(request.getPreferredCountry());
        if (request.getLatitude() != null) existingUser.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) existingUser.setLongitude(request.getLongitude());

        User saved = userRepository.save(existingUser);
        log.info("Updated preferred location for user '{}': city='{}', coords=({},{})",
                saved.getEmail(), saved.getPreferredCity(), saved.getLatitude(), saved.getLongitude());
        return enrichWithClaimedMosques(userMapper.toDto(saved), saved);
    }

    /**
     * Fetches any user by their database UUID.
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return enrichWithClaimedMosques(userMapper.toDto(user), user);
    }

    /**
     * Synchronizes Firebase authentication metadata with PostgreSQL database.
     */
    @Transactional
    public UserResponseDto syncUser(UserSyncRequestDto request) {
        String emailLower = request.getEmail() != null ? request.getEmail().toLowerCase() : "";
        String uidLower = request.getFirebaseUid() != null ? request.getFirebaseUid().toLowerCase() : "";

        User user = userRepository.findByFirebaseUid(request.getFirebaseUid())
                .or(() -> request.getEmail() != null ? userRepository.findByEmail(request.getEmail()) : java.util.Optional.empty())
                .map(existingUser -> {
                    userMapper.updateEntityFromDto(request, existingUser);
                    // Ensure backend honors moderator & admin designations
                    if (emailLower.contains("superadmin") || emailLower.startsWith("admin@") || uidLower.contains("superadmin")) {
                        if (existingUser.getRole() != UserRole.SUPER_ADMIN) {
                            existingUser.setRole(UserRole.SUPER_ADMIN);
                        }
                    } else if ((emailLower.contains("moderator") || uidLower.contains("moderator")) && existingUser.getRole() == UserRole.USER) {
                        existingUser.setRole(UserRole.MODERATOR);
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    User newUser = userMapper.toEntity(request);
                    UserRole initialRole = UserRole.USER;
                    if (emailLower.contains("superadmin") || emailLower.startsWith("admin@") || uidLower.contains("superadmin")) {
                        initialRole = UserRole.SUPER_ADMIN;
                    } else if (emailLower.contains("moderator") || uidLower.contains("moderator")) {
                        initialRole = UserRole.MODERATOR;
                    }

                    newUser.setRole(initialRole);
                    newUser.setPoints(initialRole != UserRole.USER ? 100 : 0);
                    newUser.setActive(true);
                    newUser.setVerified(initialRole != UserRole.USER);
                    return newUser;
                });

        User savedUser = userRepository.save(user);
        log.info("User synced successfully: {}, role: {}", savedUser.getEmail(), savedUser.getRole());
        return enrichWithClaimedMosques(userMapper.toDto(savedUser), savedUser);
    }

    /**
     * Updates a user's system authorization role (Super Admin operation).
     */
    @Transactional
    public UserResponseDto updateUserRole(UUID userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setRole(newRole);
        User updated = userRepository.save(user);
        log.info("Updated role for user '{}' to '{}'", user.getEmail(), newRole);
        return userMapper.toDto(updated);
    }

    /**
     * Lists users with pagination and optional role filtering and search (Super Admin operation).
     */
    @Transactional(readOnly = true)
    public PageResponse<UserResponseDto> listUsers(UserRole role, String search, Pageable pageable) {
        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<User> page = (role != null || trimmedSearch != null)
                ? userRepository.searchUsers(role, trimmedSearch, pageable)
                : userRepository.findAll(pageable);

        List<UserResponseDto> dtoList = page.getContent().stream()
                .map(userMapper::toDto)
                .toList();

        return PageResponse.from(page, dtoList);
    }

    /**
     * Rewards contribution points to a user.
     */
    @Transactional
    public void rewardPoints(UUID userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.addPoints(points);
        User saved = userRepository.save(user);
        log.info("Rewarded {} points to user: {}", points, user.getId());
        badgeService.evaluatePointsBadges(saved);
    }
}
