package aico.backend.curriculum.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurriculumStep {
    private String description;
    private Boolean completed;
    private String recommendation;
}
