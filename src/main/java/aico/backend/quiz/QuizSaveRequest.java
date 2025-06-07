package aico.backend.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizSaveRequest {
    private String imageUrl;
    private String fileName;
    private String originalFileName;
    private String quiz;
    private String answer;
}



