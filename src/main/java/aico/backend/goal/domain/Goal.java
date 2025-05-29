package aico.backend.goal.domain;

import aico.backend.curriculum.domain.Curriculum;
import aico.backend.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "goals") //데이터베이스 테이블 이름을 goals로 지정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) //User.java와 유사하게 기본 생성자 접근 레벨 설정
@Builder
@AllArgsConstructor
public class Goal {

    //기본키(PK)로 사용할 필드. DB에서는 이 값을 기준으로 각 데이터를 구분
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //DB에서 자동으로 번호 증가
    @Column(name = "goal_id") //데이터베이스 column명 goal_id로 지정
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

    //User과의 관계 (N:1)
    //한 명의 User는 여러 Goal을 가질 수 있다. Goal은 반드시 한 명의 User에게 속한다.
    @ManyToOne(fetch = FetchType.LAZY) //LAZY: Goal을 조회할 때 User정보를 바로 읽지 않음 (성능 최적화)
    @JoinColumn(name = "user_id", nullable = false) //외래 키 컬럼 이름 지정
    private User user;

    //Curriculum과의 관계 (N:1, null도 가능)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id") //외래 키 컬럼 이름 지정
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
