package aico.backend.curriculum;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CurriculumStep {
    private String description;
    private boolean completed;
}
