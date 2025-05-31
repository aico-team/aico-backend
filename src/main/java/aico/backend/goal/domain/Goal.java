package aico.backend.goal.domain;

import aico.backend.curriculum.domain.Curriculum;
import aico.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "goals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long goalId;

    //완료 상태를 나타내는 boolean 변수
    @Column(nullable = false)
    @Builder.Default
    private boolean completed  = false; //기본값은 false로, 목표가 완료되지않았다는 것을 의미

    //목표 이름
    @Column(nullable = false)
    private String goalName;

    //목표 마감일
    private LocalDate deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    //Curriculum과의 관계 (N:1, null도 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id")
    private Curriculum curriculum;


    //Goal 수정 메소드
    public void updateGoal(String newGoalName, LocalDate newDeadline, Curriculum newCurriculum, boolean newCompletedStatus) {
        //DTO에서 @NotBlank 으로 검증 됐다고 가정
        this.goalName = newGoalName;

        this.deadline = newDeadline;
        this.curriculum = newCurriculum;

        this.completed = newCompletedStatus;
    }

    public void toggleCompletion() {
        this.completed = !this.completed;
    }
    
    //Goal과 Curriculum간의 관계를 없애는 메서드: Goal의 관계 Curriculum을 없애고 싶을 때 사용
    public void clearCurriculum() {
        this.curriculum = null;
    }

}
