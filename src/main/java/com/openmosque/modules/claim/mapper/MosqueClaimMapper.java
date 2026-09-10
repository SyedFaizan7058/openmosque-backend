package com.openmosque.modules.claim.mapper;

import com.openmosque.modules.claim.dto.MosqueClaimResponseDto;
import com.openmosque.modules.claim.dto.MosqueClaimSubmitDto;
import com.openmosque.modules.claim.entity.MosqueClaimRequest;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MosqueClaimMapper {

    @Mapping(target = "mosqueId", source = "mosque.id")
    @Mapping(target = "mosqueName", source = "mosque.name")
    @Mapping(target = "claimantId", source = "claimant.id")
    @Mapping(target = "claimantEmail", source = "claimant.email")
    @Mapping(target = "reviewerId", source = "reviewer.id")
    @Mapping(target = "reviewerName", source = "reviewer.displayName")
    MosqueClaimResponseDto toDto(MosqueClaimRequest request);

    List<MosqueClaimResponseDto> toDtoList(List<MosqueClaimRequest> requests);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mosque", ignore = true)
    @Mapping(target = "claimant", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "reviewComments", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    MosqueClaimRequest toEntity(MosqueClaimSubmitDto dto);
}
