package aico.backend.goal.controller;


import aico.backend.goal.dto.GoalCreateRequestDto;
import aico.backend.goal.dto.GoalResponseDto;
import aico.backend.goal.dto.GoalUpdateRequestDto;
import aico.backend.goal.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<GoalResponseDto> createGoal(
            @Valid @RequestBody GoalCreateRequestDto requestDto,
            @RequestParam Long userId //추후 인증된 사용자 ID 가져오도록 수정
    ) {
        GoalResponseDto createdGoal = goalService.createGoal(requestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGoal);
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<GoalResponseDto> getGoalById(
            @PathVariable Long goalId,
            @RequestParam Long userId
    ) {
        GoalResponseDto goalResponseDto = goalService.getGoalById(goalId, userId);
        return ResponseEntity.ok(goalResponseDto);
    }

    @GetMapping
    public ResponseEntity<List<GoalResponseDto>> getAllGoalsForUser(
            @RequestParam Long userId
    ) {
        List<GoalResponseDto> goals = goalService.getAllGoalsForUser(userId);
        return ResponseEntity.ok(goals);
    }

    @PutMapping("/{goalId}")
    public ResponseEntity<GoalResponseDto> updateGoal(
            @PathVariable Long goalId,
            @Valid @RequestBody GoalUpdateRequestDto requestDto,
            @RequestParam Long userId
    ) {
        GoalResponseDto updatedGoal = goalService.updateGoal(goalId, requestDto, userId);
        return ResponseEntity.ok(updatedGoal);
    }

    @DeleteMapping("/{goldId}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable Long goalId,
            @RequestParam Long userId
    ) {
        goalService.deleteGoal(goalId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{goalId}/toggle-complete")
    public ResponseEntity<GoalResponseDto> toggleGoalCompletionStatus(
            @PathVariable Long goalId,
            @RequestParam Long userId
    ) {
        GoalResponseDto updatedGoal = goalService.toggleGoalCompletionStatus(goalId, userId);
        return ResponseEntity.ok(updatedGoal);
    }

}
