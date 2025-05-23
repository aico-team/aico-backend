package aico.backend.curriculum.dto;

import lombok.Data;

@Data
public class CompletionDto {
    private Long id;
    private String stage;
    private Boolean completed;
}
