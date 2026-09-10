package com.openmosque.modules.mosque.mapper;

import com.openmosque.modules.mosque.dto.*;
import com.openmosque.modules.mosque.entity.Facility;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.entity.MosqueFacility;
import com.openmosque.modules.mosque.entity.MosqueImage;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct Mapper for Mosque and Facility entities and DTOs.
 */
@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MosqueMapper {

    FacilityDto toDto(Facility facility);

    List<FacilityDto> toFacilityDtoList(List<Facility> facilities);

    MosqueImageDto toDto(MosqueImage image);

    List<MosqueImageDto> toImageDtoList(List<MosqueImage> images);

    @Mapping(target = "facilityId", source = "facility.id")
    @Mapping(target = "facilityCode", source = "facility.code")
    @Mapping(target = "facilityName", source = "facility.name")
    @Mapping(target = "iconName", source = "facility.iconName")
    MosqueFacilityDto toDto(MosqueFacility mosqueFacility);

    List<MosqueFacilityDto> toMosqueFacilityDtoList(List<MosqueFacility> mosqueFacilities);

    MosqueResponseDto toResponseDto(Mosque mosque);

    @Mapping(target = "distanceKm", ignore = true)
    @Mapping(target = "coverImageUrl", expression = "java(extractCoverImageUrl(mosque))")
    @Mapping(target = "facilityCodes", expression = "java(extractFacilityCodes(mosque))")
    MosqueSummaryDto toSummaryDto(Mosque mosque);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "facilities", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Mosque toEntity(MosqueCreateRequestDto dto);

    default String extractCoverImageUrl(Mosque mosque) {
        if (mosque.getImages() == null || mosque.getImages().isEmpty()) {
            return null;
        }
        return mosque.getImages().stream()
                .filter(MosqueImage::isCover)
                .findFirst()
                .map(MosqueImage::getImageUrl)
                .orElseGet(() -> mosque.getImages().get(0).getImageUrl());
    }

    default List<String> extractFacilityCodes(Mosque mosque) {
        if (mosque.getFacilities() == null || mosque.getFacilities().isEmpty()) {
            return List.of();
        }
        return mosque.getFacilities().stream()
                .map(mf -> mf.getFacility().getCode())
                .toList();
    }
}
