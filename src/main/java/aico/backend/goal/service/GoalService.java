package aico.backend.goal.service;

import aico.backend.curriculum.domain.Curriculum;
import aico.backend.curriculum.repository.CurriRepository;
import aico.backend.goal.domain.Goal;
import aico.backend.goal.dto.GoalCreateRequestDto;
import aico.backend.goal.dto.GoalResponseDto;
import aico.backend.goal.dto.GoalUpdateRequestDto;
import aico.backend.goal.repository.GoalRepository;
import aico.backend.user.domain.User;
import aico.backend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    private final CurriRepository curriRepository;

    @Transactional
    public GoalResponseDto createGoal(GoalCreateRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("요청하신 사용자를 찾을 수 없습니다. ID: " + userId));

        Curriculum curriculum = null;
        if (requestDto.getCurrId() != null) {
            curriculum = curriRepository.findById(requestDto.getCurrId())
                    .orElseThrow(() -> new EntityNotFoundException("요청하신 커리큘럼을 찾을 수 없습니다. ID: " + requestDto.getCurrId()));
        }

        Goal newGoal = Goal.builder() //새로 생성할 때 초기화 builder
                .goalName(requestDto.getGoalName())
                .deadline(requestDto.getDeadline())
                .user(user)
                .curriculum(curriculum)
                .completed(false) //이미 false로 설정되어있지만 명시적으로 설정
                .build();

        Goal savedGoal = goalRepository.save(newGoal); //새로 만들어진 goal을 저장시켜놓는

        return GoalResponseDto.fromEntity(savedGoal);

    }

    //goalId로 goal찾기
    @Transactional (readOnly = true)
    public GoalResponseDto getGoalById(Long goalId, Long userId) {
        //1. 목표 조회
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("목표를 찾을 수 없습니다. ID: " + goalId));

        //2. 목표 소유자 확인 (권한 검사)
        if (!goal.getUser().getId().equals(userId)) {
            throw new SecurityException("해당 목표에 접근할 권한이 없습니다.");
        }

        //3. 응답 DTO로 변환하여 반환
        return GoalResponseDto.fromEntity(goal);
    }

    //사용자의 모든 목표 조회
    @Transactional (readOnly = true) //test를 편하게..
    public List<GoalResponseDto> getAllGoalsForUser(Long userId) {
        //1. 사용자 엔티티 조회 (해당 사용자가 존재하는지 확인)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        //2. 해당 사용자의 모든 목표 조회
        List<Goal> goals = goalRepository.findByUser(user);

        //3. 각 Goal 엔티티를 GoalResponseDto로 변환하여 리스트로 만들기
        return goals.stream()
                .map(GoalResponseDto::fromEntity)
                .collect(Collectors.toUnmodifiableList());

    }

    @Transactional
    public GoalResponseDto updateGoal(Long goalId, GoalUpdateRequestDto requestDto, Long userId) {
        //1. 목표 조회
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 목표를 찾을 수 없습니다. ID: " + goalId));

        //2. 목표 소유자 확인 (권한 검사)
        if (!goal.getUser().getId().equals(userId)) {
            throw new SecurityException("해당 목표를 수정할 권한이 없습니다.");

        }

        //3. DTO의 값으로 엔티티 필드 없데이트 (전체 업데이트)
        Curriculum newCurriculum = null;

        Long curriculumIdFromDto = requestDto.getCurrId();

        if (curriculumIdFromDto != null) {
            if (curriculumIdFromDto.equals(0L)) {
                newCurriculum = null;
            } else {
                newCurriculum = curriRepository.findById(curriculumIdFromDto)
                        .orElseThrow(() -> new EntityNotFoundException("연결할 커리큘럼을 찾을 수 없습니다. ID: " + curriculumIdFromDto));
            }
        }
        goal.updateGoal(
                requestDto.getGoalName(),
                requestDto.getDeadline(),
                newCurriculum,
                requestDto.isCompleted()
        );

        //4. @Transactional에 의해 변경된 엔티티는 트랜잭션 커밋 시점에 DB에 반영

        //5. 응답 DTO로 변환하여 반환
        return GoalResponseDto.fromEntity(goal);
    }

    @Transactional
    public void deleteGoal(Long goalId, Long userId) {
        //1. 목표 조회
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 목표를 찾을 수 없습니다. ID: " + goalId));

        //2. 목표 소유자 확인 (권한 검사)
        if (!goal.getUser().getId().equals(userId)) {
            throw new SecurityException("해당 목표를 삭제할 권한이 없습니다.");
        }

        //3. 목표 삭제
        goalRepository.delete(goal);
    }

    @Transactional
    public GoalResponseDto toggleGoalCompletionStatus(Long goalId, Long userId) {
        //1. 목표 조회
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new EntityNotFoundException("상태를 변경할 목표를 찾을 수 없습니다. ID: " + goalId));

        //2. 목표 소유자 확인 (권한 검사)
        if (!goal.getUser().getId().equals(userId)) {
            throw new SecurityException("해당 목표의 상태를 변경할 권한이 없습니다.");
        }

        //3. 완료 상태 토글
        goal.toggleCompletion();

        //4. 변경된 Goal 엔티티는 @Transactional에 의해 DB에 반영.

        //5. 응답 DTO로 변환하여 반환
        return GoalResponseDto.fromEntity(goal);
    }

}
