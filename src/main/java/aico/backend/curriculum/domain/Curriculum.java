package aico.backend.curriculum.domain;

import aico.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(schema = "aico")
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

    private Double progress;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> recommendations;

    @Builder
    public Curriculum(String topic, User user, Map<String, CurriculumStep> curriculumMap,
                      Double progress, Map<String, String> recommendations) {
        this.topic = topic;
        this.user = user;
        this.curriculumMap = curriculumMap;
        this.progress = progress;
        this.recommendations = recommendations;
    }

    public String getStepDescription(String step) {
        return this.curriculumMap.get(step).getDescription();
    }

    public void changeCompletion(String step, Boolean completed) {
        this.curriculumMap.get(step).setCompleted(completed);
    }

    public Double changeProgress(Double progress) {
        this.progress = progress;
        return this.progress;
    }

    public void changeRecommend(String step, String value) {
        this.recommendations.put(step, value);
    }

}
