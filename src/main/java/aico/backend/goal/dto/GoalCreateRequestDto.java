package aico.backend.goal.dto;

//새로운 목표를 생성할 때 필요한 정보 (userId는 JWT 토큰을 통해 서버에서 직접)

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class GoalCreateRequestDto {

    @NotBlank(message = "목표명은 필수입니다.")
    @Size(max = 100, message = "목표명은 100자 이하로 입력해주세요.")
    private String goalName;

    @FutureOrPresent(message = "마감일은 과거가 될 수 없습니다.")
    private LocalDate deadline;

    private Long currId;

    public GoalCreateRequestDto(String goalName, LocalDate deadline, Long currId) {
        this.goalName = goalName;
        this.deadline = deadline;
        this.currId = currId;
    }

}
