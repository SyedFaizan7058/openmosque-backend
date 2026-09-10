package com.openmosque.common.util;

import com.openmosque.common.exception.ForbiddenException;
import com.openmosque.common.exception.UnauthorizedException;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Centralized Role-Based Access Control (RBAC) Security Utility.
 *
 * WHY THIS IS WRITTEN:
 * Eliminates repetitive boilerplate role-checking logic across controllers and services.
 * Ensures consistent security exception messages (ForbiddenException vs UnauthorizedException)
 * and centralizes authorization policies in one reusable location.
 *
 * WHERE IT IS USED:
 * - MosqueEventService (checks event creation and modification privileges)
 * - MosqueKhutbahService (checks Friday Khutbah scheduling permissions)
 * - PrayerTimesService (checks Iqamah timetable overrides)
 * - CommunityReviewService (ensures users can only edit or delete their own reviews)
 * - ModerationService (ensures only MODERATOR or SUPER_ADMIN can review queues)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUtils {

    /**
     * Validates that the provided user has either MOSQUE_ADMIN or SUPER_ADMIN role.
     *
     * @param user Authenticated user injected via @CurrentUser
     * @param actionDescription Human-readable action description for audit logs and error messages
     * @throws UnauthorizedException if user is null (unauthenticated)
     * @throws ForbiddenException if user lacks required administrative privileges
     */
    public static void requireMosqueAdminOrSuperAdmin(User user, String actionDescription) {
        if (user == null) {
            throw new UnauthorizedException("Authentication is required to " + actionDescription);
        }
        if (user.getRole() == UserRole.SUPER_ADMIN || user.getRole() == UserRole.MOSQUE_ADMIN) {
            return;
        }
        throw new ForbiddenException(
                String.format("Access denied: You must be a verified MOSQUE_ADMIN or SUPER_ADMIN to %s.", actionDescription)
        );
    }

    /**
     * Validates that the provided user has either MODERATOR or SUPER_ADMIN role.
     *
     * @param user Authenticated user
     * @param actionDescription Action description
     * @throws ForbiddenException if user lacks moderation privileges
     */
    public static void requireModeratorOrSuperAdmin(User user, String actionDescription) {
        if (user == null) {
            throw new UnauthorizedException("Authentication is required to " + actionDescription);
        }
        if (user.getRole() == UserRole.SUPER_ADMIN || user.getRole() == UserRole.MODERATOR) {
            return;
        }
        throw new ForbiddenException(
                String.format("Access denied: You must be a MODERATOR or SUPER_ADMIN to %s.", actionDescription)
        );
    }

    /**
     * Validates that the action is performed by either the resource owner or a SUPER_ADMIN.
     *
     * @param currentUser Authenticated user performing the request
     * @param ownerId UUID of the user who owns the resource
     * @param resourceName Human-readable resource name (e.g. "review", "submission")
     * @throws ForbiddenException if user is not the owner and not a super admin
     */
    public static void requireOwnerOrSuperAdmin(User currentUser, UUID ownerId, String resourceName) {
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication is required.");
        }
        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }
        if (ownerId != null && ownerId.equals(currentUser.getId())) {
            return;
        }
        throw new ForbiddenException(
                String.format("Access denied: You can only modify your own %s.", resourceName)
        );
    }

    /**
     * Validates that content deletion is performed by:
     * 1. The original content author/owner
     * 2. A MODERATOR or SUPER_ADMIN
     * 3. A MOSQUE_ADMIN for the target mosque
     *
     * @param currentUser Authenticated user performing the delete request
     * @param ownerId UUID of the user who authored the content
     * @param isMosqueAdminForMosque true if user is verified admin of this specific mosque
     * @param resourceName Human-readable resource name
     */
    public static void requireDeletePermission(User currentUser, UUID ownerId, boolean isMosqueAdminForMosque, String resourceName) {
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication is required.");
        }
        // Super Admin and Platform Moderators can delete inappropriate content platform-wide
        if (currentUser.getRole() == UserRole.SUPER_ADMIN || currentUser.getRole() == UserRole.MODERATOR) {
            return;
        }
        // Original Author can delete their own content
        if (ownerId != null && ownerId.equals(currentUser.getId())) {
            return;
        }
        // Verified Mosque Admin can delete content within their mosque
        if (isMosqueAdminForMosque && currentUser.getRole() == UserRole.MOSQUE_ADMIN) {
            return;
        }
        throw new ForbiddenException(
                String.format("Access denied: You do not have permission to delete this %s.", resourceName)
        );
    }
}
