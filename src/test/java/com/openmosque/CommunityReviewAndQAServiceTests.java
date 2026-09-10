package com.openmosque;

import com.openmosque.common.exception.BadRequestException;
import com.openmosque.common.exception.ConflictException;
import com.openmosque.common.util.GeoUtils;
import com.openmosque.modules.community.dto.*;
import com.openmosque.modules.community.entity.ContentStatus;
import com.openmosque.modules.community.entity.FlagStatus;
import com.openmosque.modules.community.entity.TargetType;
import com.openmosque.modules.community.repository.CommunityContentFlagRepository;
import com.openmosque.modules.community.repository.MosqueAnswerRepository;
import com.openmosque.modules.community.repository.MosqueQuestionRepository;
import com.openmosque.modules.community.repository.MosqueReviewRepository;
import com.openmosque.modules.community.service.CommunityModerationService;
import com.openmosque.modules.community.service.CommunityQAService;
import com.openmosque.modules.community.service.CommunityReviewService;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.mosque.repository.MosqueRepository;
import com.openmosque.modules.user.entity.User;
import com.openmosque.modules.user.entity.UserRole;
import com.openmosque.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CommunityReviewAndQAServiceTests {

    @Autowired
    private CommunityReviewService reviewService;

    @Autowired
    private CommunityQAService qaService;

    @Autowired
    private CommunityModerationService moderationService;

    @Autowired
    private MosqueReviewRepository reviewRepository;

    @Autowired
    private MosqueQuestionRepository questionRepository;

    @Autowired
    private MosqueAnswerRepository answerRepository;

    @Autowired
    private CommunityContentFlagRepository flagRepository;

    @Autowired
    private MosqueRepository mosqueRepository;

    @Autowired
    private UserRepository userRepository;

    private User regularUser;
    private User secondUser;
    private User imamUser;
    private User moderatorUser;
    private Mosque testMosque;

    @BeforeEach
    void setUp() {
        flagRepository.deleteAll();
        answerRepository.deleteAll();
        questionRepository.deleteAll();
        reviewRepository.deleteAll();
        mosqueRepository.deleteAll();

        String salt = UUID.randomUUID().toString().substring(0, 8);

        regularUser = userRepository.save(User.builder()
                .firebaseUid("user1-" + salt)
                .email("user1-" + salt + "@openmosque.org")
                .displayName("Brother Bilal")
                .role(UserRole.USER)
                .active(true)
                .build());

        secondUser = userRepository.save(User.builder()
                .firebaseUid("user2-" + salt)
                .email("user2-" + salt + "@openmosque.org")
                .displayName("Sister Zaynab")
                .role(UserRole.USER)
                .active(true)
                .build());

        imamUser = userRepository.save(User.builder()
                .firebaseUid("imam-" + salt)
                .email("imam-" + salt + "@openmosque.org")
                .displayName("Imam Tariq")
                .role(UserRole.MOSQUE_ADMIN)
                .active(true)
                .build());

        moderatorUser = userRepository.save(User.builder()
                .firebaseUid("mod-" + salt)
                .email("mod-" + salt + "@openmosque.org")
                .displayName("Moderator Farooq")
                .role(UserRole.MODERATOR)
                .active(true)
                .build());

        testMosque = mosqueRepository.save(Mosque.builder()
                .name("Oxford Islamic Center")
                .slug("oxford-islamic-center-" + salt)
                .address("Marston Road")
                .city("Oxford")
                .country("United Kingdom")
                .latitude(51.7520)
                .longitude(-1.2577)
                .location(GeoUtils.createPoint(51.7520, -1.2577))
                .verified(true)
                .build());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        flagRepository.deleteAll();
        answerRepository.deleteAll();
        questionRepository.deleteAll();
        reviewRepository.deleteAll();
        mosqueRepository.deleteAll();
    }

    @Test
    @DisplayName("Should submit review, prevent duplicates, and calculate rating summary")
    void testSubmitReviewAndCalculateRatings() {
        ReviewCreateDto dto = ReviewCreateDto.builder()
                .ratingOverall(5)
                .ratingCleanliness(5)
                .ratingFacilities(4)
                .ratingWomensArea(5)
                .ratingParking(3)
                .reviewText("Beautiful mosque with great community activities.")
                .build();

        ReviewResponseDto created = reviewService.createReview(testMosque.getId(), dto, regularUser);

        assertThat(created).isNotNull();
        assertThat(created.getRatingOverall()).isEqualTo(5);
        assertThat(created.getStatus()).isEqualTo(ContentStatus.PUBLISHED);

        // Duplicate submission should throw ConflictException
        assertThatThrownBy(() -> reviewService.createReview(testMosque.getId(), dto, regularUser))
                .isInstanceOf(ConflictException.class);

        // Rating summary check
        RatingSummaryDto summary = reviewService.getRatingSummary(testMosque.getId());
        assertThat(summary.getTotalReviews()).isEqualTo(1);
        assertThat(summary.getAverageOverall()).isEqualTo(5.0);
        assertThat(summary.getAverageCleanliness()).isEqualTo(5.0);
        assertThat(summary.getAverageParking()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("Profanity in review should be rejected with BadRequestException")
    void testProfanityFilterRejectsReview() {
        ReviewCreateDto dto = ReviewCreateDto.builder()
                .ratingOverall(1)
                .reviewText("This place is full of shit and trash.")
                .build();

        assertThatThrownBy(() -> reviewService.createReview(testMosque.getId(), dto, regularUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("prohibited or offensive language");
    }

    @Test
    @DisplayName("Community Q&A should award official badge when answered by Mosque Admin")
    void testCommunityQAWithOfficialBadge() {
        QuestionCreateDto qDto = QuestionCreateDto.builder()
                .questionText("Is there parking available during Friday prayers?")
                .build();

        QuestionResponseDto question = qaService.createQuestion(testMosque.getId(), qDto, regularUser);
        assertThat(question).isNotNull();
        assertThat(question.getQuestionText()).contains("parking");

        AnswerCreateDto aDto = AnswerCreateDto.builder()
                .answerText("Yes, our underground car park is open from 12:00 PM on Fridays.")
                .build();

        AnswerResponseDto answer = qaService.createAnswer(question.getId(), aDto, imamUser);

        assertThat(answer).isNotNull();
        assertThat(answer.isOfficialMosqueAdmin()).isTrue();

        Page<QuestionResponseDto> threads = qaService.getQuestions(testMosque.getSlug(), PageRequest.of(0, 10));
        assertThat(threads.getContent()).hasSize(1);
        assertThat(threads.getContent().get(0).getAnswers()).hasSize(1);
        assertThat(threads.getContent().get(0).getAnswers().get(0).isOfficialMosqueAdmin()).isTrue();
    }

    @Test
    @DisplayName("Reporting review should allow moderator to resolve flag and hide content")
    void testFlagReviewAndModeratorResolve() {
        ReviewCreateDto dto = ReviewCreateDto.builder()
                .ratingOverall(2)
                .reviewText("Terrible experience, not recommended.")
                .build();

        ReviewResponseDto review = reviewService.createReview(testMosque.getId(), dto, regularUser);

        // Second user flags the review
        ContentFlagCreateDto flagDto = ContentFlagCreateDto.builder()
                .targetType(TargetType.REVIEW)
                .targetId(review.getId())
                .reason("Fake review from competitor")
                .build();

        ContentFlagResponseDto flag = moderationService.flagContent(flagDto, secondUser);
        assertThat(flag.getStatus()).isEqualTo(FlagStatus.PENDING);

        // Moderator decides flag
        FlagDecisionDto decision = FlagDecisionDto.builder()
                .status(FlagStatus.RESOLVED)
                .reviewerNotes("Confirmed fake review. Hiding review.")
                .build();

        ContentFlagResponseDto decided = moderationService.decideFlag(flag.getId(), decision, moderatorUser);
        assertThat(decided.getStatus()).isEqualTo(FlagStatus.RESOLVED);

        // Target review should now be HIDDEN and excluded from public listings
        Page<ReviewResponseDto> publicReviews = reviewService.getReviews(testMosque.getSlug(), PageRequest.of(0, 10));
        assertThat(publicReviews.getContent()).isEmpty();
    }
}
