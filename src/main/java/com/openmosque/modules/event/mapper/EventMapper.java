package com.openmosque.modules.event.mapper;

import com.openmosque.modules.event.dto.MosqueEventCreateDto;
import com.openmosque.modules.event.dto.MosqueEventResponseDto;
import com.openmosque.modules.event.dto.MosqueKhutbahCreateDto;
import com.openmosque.modules.event.dto.MosqueKhutbahResponseDto;
import com.openmosque.modules.event.entity.MosqueEvent;
import com.openmosque.modules.event.entity.MosqueKhutbah;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * <h3>EventMapper</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * MapStruct compile-time mapper providing type-safe, zero-boilerplate transformations between
 * {@link MosqueEvent} / {@link MosqueKhutbah} JPA entities and their respective REST DTOs.
 * Prevents repetitive manual bean copying.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Injected into {@link com.openmosque.modules.event.service.MosqueEventService}
 * and {@link com.openmosque.modules.event.service.MosqueKhutbahService}.
 * </p>
 */
@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EventMapper {

    @Mapping(target = "mosqueId", source = "mosque.id")
    @Mapping(target = "mosqueName", source = "mosque.name")
    MosqueEventResponseDto toDto(MosqueEvent event);

    List<MosqueEventResponseDto> toEventDtoList(List<MosqueEvent> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mosque", ignore = true)
    @Mapping(target = "cancelled", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    MosqueEvent toEntity(MosqueEventCreateDto dto);

    @Mapping(target = "mosqueId", source = "mosque.id")
    @Mapping(target = "mosqueName", source = "mosque.name")
    MosqueKhutbahResponseDto toDto(MosqueKhutbah khutbah);

    List<MosqueKhutbahResponseDto> toKhutbahDtoList(List<MosqueKhutbah> khutbahs);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mosque", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    MosqueKhutbah toEntity(MosqueKhutbahCreateDto dto);
}
