package com.openmosque.modules.community.mapper;

import com.openmosque.modules.community.dto.*;
import com.openmosque.modules.community.entity.CommunityContentFlag;
import com.openmosque.modules.community.entity.MosqueAnswer;
import com.openmosque.modules.community.entity.MosqueQuestion;
import com.openmosque.modules.community.entity.MosqueReview;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * <h3>CommunityMapper</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * MapStruct compile-time mapper providing type-safe, zero-boilerplate transformations between
 * community JPA entities ({@link MosqueReview}, {@link MosqueQuestion}, {@link MosqueAnswer},
 * {@link CommunityContentFlag}) and their respective client REST DTOs. Automatically maps nested user
 * profile information (id, displayName, photoUrl) without manual boilerplate.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * Injected into {@link com.openmosque.modules.community.service.CommunityReviewService},
 * {@link com.openmosque.modules.community.service.CommunityQAService},
 * and {@link com.openmosque.modules.community.service.CommunityModerationService}.
 * </p>
 */
@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CommunityMapper {

    @Mapping(target = "mosqueId", source = "mosque.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userDisplayName", source = "user.displayName")
    @Mapping(target = "userPhotoUrl", source = "user.photoUrl")
    ReviewResponseDto toDto(MosqueReview review);

    List<ReviewResponseDto> toReviewDtoList(List<MosqueReview> reviews);

    @Mapping(target = "mosqueId", source = "mosque.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userDisplayName", source = "user.displayName")
    @Mapping(target = "userPhotoUrl", source = "user.photoUrl")
    @Mapping(target = "answers", ignore = true)
    QuestionResponseDto toDto(MosqueQuestion question);

    List<QuestionResponseDto> toQuestionDtoList(List<MosqueQuestion> questions);

    @Mapping(target = "questionId", source = "question.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userDisplayName", source = "user.displayName")
    @Mapping(target = "userPhotoUrl", source = "user.photoUrl")
    AnswerResponseDto toDto(MosqueAnswer answer);

    List<AnswerResponseDto> toAnswerDtoList(List<MosqueAnswer> answers);

    @Mapping(target = "reporterId", source = "reporter.id")
    @Mapping(target = "reporterEmail", source = "reporter.email")
    @Mapping(target = "reviewerId", source = "reviewer.id")
    ContentFlagResponseDto toDto(CommunityContentFlag flag);

    List<ContentFlagResponseDto> toFlagDtoList(List<CommunityContentFlag> flags);
}
