package com.openmosque.modules.user.dto;

import com.openmosque.modules.user.entity.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleUpdateRequestDto {

    @NotNull(message = "Role is required (USER, MOSQUE_ADMIN, MODERATOR, SUPER_ADMIN)")
    private UserRole role;
}
