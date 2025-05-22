package aico.backend.studytime.repository;

import aico.backend.studytime.domain.StudyTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyTimeRepository extends JpaRepository<StudyTime, Long> {
    Optional<StudyTime> findByUserIdAndDate(Long userId, LocalDate date);

    List<StudyTime> findByUserIdAndDateBetween(Long userId, LocalDate start, LocalDate end);

    List<StudyTime> findByUserIdAndDateBeforeOrderByDateDesc(Long userId, LocalDate today);
}
