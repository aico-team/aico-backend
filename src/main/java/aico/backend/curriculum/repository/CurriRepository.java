package aico.backend.curriculum.repository;

import aico.backend.curriculum.domain.Curriculum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriRepository extends JpaRepository<Curriculum, Long> {
}
