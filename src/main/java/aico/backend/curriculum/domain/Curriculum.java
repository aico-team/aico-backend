package aico.backend.curriculum.domain;

import aico.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Curriculum {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, CurriculumStep> curriculumMap;

    @Builder
    public Curriculum(String topic, User user, Map<String, CurriculumStep> curriculumMap) {
        this.topic = topic;
        this.user = user;
        this.curriculumMap = curriculumMap;
    }
}
