package aico.backend.goal.repository;

import aico.backend.curriculum.domain.Curriculum;
import aico.backend.goal.domain.Goal;
import aico.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    //JpaRepository<엔티티 클래스, 엔티티의 PK타입>

    // === 기본 제공 메소드 ===
    // Goal save(Goal goal); : 저장 및 수정
    // Optional<Goal> findById(Long goalId); : ID로 조회
    // List<Goal> findAll(); : 전체 조회
    // void deleteById(Long goalId); : ID로 삭제
    // boolean existsById(Long goalId); :ID 존재 여부 확인
    // long count(); : 전체 개수


    //특정 사용자의 모든 목표 조회(User객체 사용)
    List<Goal> findByUser(User user);

    //특정 사용자의 목표 중 특정 이름을 가진 목표 조회
    Optional<Goal> findByUserAndGoalName(User user, String goalName);

    //특정 사용자의 목표 중 특정 커리큘럼에 연결된 목표 모두 조회
    List<Goal> findByUserAndCurriculum(User user, Curriculum curriculum);

    //특정 사용자의 목표 중 커리큘럼에 연결되지 않은 목표 모두 조회
    List<Goal> findByUserAndCurriculumIsNull(User user);

    //특정 사용자의 목표 중 특정 마감일 이전의 목표를 조회
    List<Goal> findByUserAndDeadlineBefore(User user, LocalDate date);

    //특정 사용자의 목표 중 특정 마감일 이후의 목표들 조회
    List<Goal> findByUserAndDeadlineAfter(User user, LocalDate date);

    //특정 사용자의 목표 중 특정 마감일의 목표를 조회
    List<Goal> findByUserAndDeadline(User user, LocalDate date);
}
