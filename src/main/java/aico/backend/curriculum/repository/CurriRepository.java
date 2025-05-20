package aico.backend.curriculum.repository;

import aico.backend.curriculum.domain.Curriculum;
import aico.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CurriRepository extends JpaRepository<Curriculum, Long> {
     List<Curriculum> findByUser(User user);

     @Query(value = """
                         SELECT COUNT(*)
                         FROM aico.curriculum, jsonb_each(aico.curriculum.curriculum_map) AS stage
                         WHERE aico.curriculum.id = :id AND (stage.value ->> 'completed')::boolean = true
             """, nativeQuery = true)
     int countCompletedStagesById(@Param("id") Long id);
}
