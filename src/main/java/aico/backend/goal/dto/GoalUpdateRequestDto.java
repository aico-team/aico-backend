package aico.backend.goal.dto;


import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class GoalUpdateRequestDto {

    @Size(max = 100, message = "목표 이름은 100자 이하로 입력해주세요.")
    @NotBlank(message = "빈 칸으로 수정할 수 없습니다.")
    private String goalName;

    @FutureOrPresent(message = "마감일은 과거의 날짜일 수 없습니다.")
    private LocalDate deadline;

    private Long currId;

    private boolean completed;

    public GoalUpdateRequestDto(String goalName, LocalDate deadline, Long currId, boolean completed) {
        this.goalName = goalName;
        this.deadline = deadline;
        this.currId = currId;
        this.completed = completed;
    }
}
