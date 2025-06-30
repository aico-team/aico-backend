package aico.backend.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WrongAnswerNoteResponse {
    private String fileName;
    private String originalFileName;
    private String imageUrl;
    private List<QuizItem> quizzes;
    private LocalDateTime createdAt;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class QuizItem {
        private Long id;
        private String quiz;
        private String answer;
        private LocalDateTime createdAt;
    }
}
