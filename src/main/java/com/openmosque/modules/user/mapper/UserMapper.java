package com.openmosque.modules.user.mapper;

import com.openmosque.modules.user.dto.UserResponseDto;
import com.openmosque.modules.user.dto.UserSyncRequestDto;
import com.openmosque.modules.user.entity.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    UserResponseDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "points", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    User toEntity(UserSyncRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firebaseUid", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "points", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDto(UserSyncRequestDto dto, @MappingTarget User user);
}
