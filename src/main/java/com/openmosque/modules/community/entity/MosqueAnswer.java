package com.openmosque.modules.community.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <h3>MosqueAnswer</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Represents answers posted in response to a {@link MosqueQuestion}. Answers can be authored by regular
 * worshippers or by verified mosque administrators. If authored by a verified administrator or imam of that mosque,
 * {@code officialMosqueAdmin} is set to {@code true}, rendering an official verified badge in the UI.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * <ul>
 *   <li>Database table: {@code mosque_answers} (created via Flyway V7 migration).</li>
 *   <li>Service layer: {@link com.openmosque.modules.community.service.CommunityQAService} for adding answers and marking questions answered.</li>
 *   <li>REST APIs: {@link com.openmosque.modules.community.controller.CommunityPublicController} and {@link com.openmosque.modules.community.controller.CommunityUserController}.</li>
 *   <li>Mapper: {@link com.openmosque.modules.community.mapper.CommunityMapper}.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "mosque_answers")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueAnswer extends BaseEntity {

    /** The question thread this answer belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private MosqueQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "answer_text", nullable = false, columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "is_official_mosque_admin", nullable = false)
    @Builder.Default
    private boolean officialMosqueAdmin = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ContentStatus status = ContentStatus.PUBLISHED;
}
