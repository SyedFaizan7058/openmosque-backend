package com.openmosque.modules.community.entity;

import com.openmosque.common.model.BaseEntity;
import com.openmosque.modules.mosque.entity.Mosque;
import com.openmosque.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * <h3>MosqueQuestion</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Represents community inquiry threads posted by worshippers inquiring about a specific mosque
 * (e.g., parking availability during Ramadan, women's entrance accessibility, janazah arrangements).
 * Maintains bidirectional relationship with {@link MosqueAnswer}.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * <ul>
 *   <li>Database table: {@code mosque_questions} (created via Flyway V7 migration).</li>
 *   <li>Service layer: {@link com.openmosque.modules.community.service.CommunityQAService} for posting, status updating, and answering.</li>
 *   <li>REST APIs: {@link com.openmosque.modules.community.controller.CommunityPublicController} (public Q&A browsing)
 *       and {@link com.openmosque.modules.community.controller.CommunityUserController} (asking questions).</li>
 *   <li>Mapper: {@link com.openmosque.modules.community.mapper.CommunityMapper}.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "mosque_questions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MosqueQuestion extends BaseEntity {

    /** The mosque about which the question is asked. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mosque_id", nullable = false)
    private Mosque mosque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private QuestionStatus status = QuestionStatus.OPEN;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MosqueAnswer> answers = new ArrayList<>();
}
