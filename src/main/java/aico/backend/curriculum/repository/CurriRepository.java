package aico.backend.curriculum.repository;

import aico.backend.curriculum.domain.Curriculum;
import aico.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CurriRepository extends JpaRepository<Curriculum, Long> {
     List<Curriculum> findByUser(User user);
}
