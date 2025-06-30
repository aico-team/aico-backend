package aico.backend.quiz;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizRepository extends CrudRepository<Quiz, Long> {
    // 특정 사용자의 모든 퀴즈를 이미지URL별로 그룹화하여 조회
    @Query("SELECT q FROM Quiz q WHERE q.user.id = :userId ORDER BY q.imageUrl, q.createdAt")
    List<Quiz> findByUserIdOrderByImageUrl(@Param("userId") Long userId);

    // 특정 사용자의 특정 이미지URL과 관련된 모든 퀴즈 조회
    List<Quiz> findByUserIdAndImageUrl(Long userId, String imageUrl);

    // 특정 사용자의 특정 파일명과 관련된 모든 퀴즈 조회
    List<Quiz> findByUserIdAndFileName(Long userId, String fileName);

    // 특정 퀴즈 삭제 (사용자 검증 포함)
    void deleteByIdAndUserId(Long id, Long userId);

    // 특정 파일명과 관련된 모든 퀴즈 삭제
    void deleteByUserIdAndFileName(Long userId, String fileName);

    // 특정 파일명과 관련된 퀴즈 개수 확인
    long countByUserIdAndFileName(Long userId, String fileName);
}

