package aico.backend.goal.controller;


import aico.backend.global.security.UserDetailsImpl;
import aico.backend.goal.dto.GoalCreateRequestDto;
import aico.backend.goal.dto.GoalResponseDto;
import aico.backend.goal.dto.GoalUpdateRequestDto;
import aico.backend.goal.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Slf4j
public class GoalController {

    private final GoalService goalService;

    // 목표 생성
    @PostMapping
    public ResponseEntity<GoalResponseDto> createGoal(
            @Valid @RequestBody GoalCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        GoalResponseDto createdGoal = goalService.createGoal(requestDto, userDetails);
        log.info("😭 골이 골이 뭐예요 골이 뭐예요? 아이디{}, 데드라인{}", createdGoal.getCurrId(), createdGoal.getDeadline() );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGoal);
    }

    // 목표 단건 조회 by Goal ID
    @GetMapping("/{goalId}")
    public ResponseEntity<GoalResponseDto> getGoalById(
            @PathVariable Long goalId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        GoalResponseDto goalResponseDto = goalService.getGoalById(goalId, userDetails);
        return ResponseEntity.ok(goalResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<GoalResponseDto>> getAllGoalsForUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<GoalResponseDto> goals = goalService.getAllGoalsForUser(userDetails);
        return ResponseEntity.ok(goals);
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<GoalResponseDto> updateGoal(
            @PathVariable Long goalId,
            @Valid @RequestBody GoalUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        GoalResponseDto updatedGoal = goalService.updateGoal(goalId, requestDto, userDetails);
        return ResponseEntity.ok(updatedGoal);
    }

    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable Long goalId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        goalService.deleteGoal(goalId, userDetails);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{goalId}/toggle-completed")
    public ResponseEntity<GoalResponseDto> toggleGoalCompletionStatus(
            @PathVariable Long goalId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        GoalResponseDto updatedGoal = goalService.toggleGoalCompletionStatus(goalId, userDetails);
        return ResponseEntity.ok(updatedGoal);
    }

}
