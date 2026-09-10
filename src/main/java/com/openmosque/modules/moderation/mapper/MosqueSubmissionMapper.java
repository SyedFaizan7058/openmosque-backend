package com.openmosque.modules.moderation.mapper;

import com.openmosque.modules.moderation.dto.MosqueSubmissionRequestDto;
import com.openmosque.modules.moderation.dto.MosqueSubmissionResponseDto;
import com.openmosque.modules.moderation.entity.MosqueSubmission;
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
public interface MosqueSubmissionMapper {

    @Mapping(target = "submitterId", source = "submitter.id")
    @Mapping(target = "submitterName", source = "submitter.displayName")
    @Mapping(target = "submitterEmail", source = "submitter.email")
    @Mapping(target = "targetMosqueId", source = "targetMosque.id")
    @Mapping(target = "reviewerId", source = "reviewer.id")
    @Mapping(target = "reviewerName", source = "reviewer.displayName")
    MosqueSubmissionResponseDto toResponseDto(MosqueSubmission submission);

    List<MosqueSubmissionResponseDto> toResponseDtoList(List<MosqueSubmission> submissions);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "submitter", ignore = true)
    @Mapping(target = "targetMosque", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reviewer", ignore = true)
    @Mapping(target = "reviewComments", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    MosqueSubmission toEntity(MosqueSubmissionRequestDto dto);
}
