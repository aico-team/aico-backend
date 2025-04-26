package aico.backend.curriculum.dto;

import aico.backend.curriculum.domain.CurriculumStep;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;


public class CurriDto {

    @Data
    public static class Request {
        @NotBlank
        private String topic;

        @NotBlank
        private String content;
    }


    @Data
    @AllArgsConstructor
    public static class Response {
        @NotBlank
        private Map<String, CurriculumStep> curriculum;
    }

}
