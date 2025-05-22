package aico.backend.goal.dto;
//서버가 클라이언트에게 목표 정보를 응답으로 보낼 때 사용


import aico.backend.goal.domain.Goal;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class GoalResponseDto {

    private Long goalId;
    private String goalName;
    private LocalDate deadline;
    private Long userId;
    private Long currId;
    private boolean completed;


    @Builder //빌더 패턴: 생성자를 깔끔하게, 필드를 선택적으로 초기화
    public GoalResponseDto(Long goalId, String goalName, LocalDate deadline, Long userId, Long currId, boolean completed){
        this.goalId = goalId;
        this.goalName = goalName;
        this.deadline = deadline;
        this.userId = userId;
        this.currId = currId;
        this.completed = completed;
    }

    // GoalResponseDto.java 내부에 위치

    public static GoalResponseDto fromEntity(Goal goal) {
        // User ID를 안전하게 가져오기
        Long actualUserId = null;
        if (goal.getUser() != null) {
            actualUserId = goal.getUser().getId(); // User 엔티티의 PK 필드명 'id'와 @Getter를 통해 생성된 getId() 사용
        }

        // Curriculum ID를 안전하게 가져오기
        Long actualCurrId = null;
        if (goal.getCurriculum() != null) {
            actualCurrId = goal.getCurriculum().getId(); // Curriculum 엔티티의 PK 필드명 'id'와 @Getter를 통해 생성된 getId() 사용
        }

        return GoalResponseDto.builder()
                .goalId(goal.getGoalId())       // Goal 엔티티의 getGoalId()
                .goalName(goal.getGoalName())   // Goal 엔티티의 getGoalName()
                .deadline(goal.getDeadline())   // Goal 엔티티의 getDeadline()
                .userId(actualUserId)           // 위에서 가져온 User ID
                .currId(actualCurrId)
                .completed(goal.isCompleted())// 위에서 가져온 Curriculum ID (DTO 필드명 currId)
                .build();
    }


}
