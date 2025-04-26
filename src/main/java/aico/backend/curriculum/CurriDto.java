package aico.backend.curriculum;

import jakarta.validation.constraints.*;
import lombok.Data;


public class CurriDto {

    @Data
    public static class Request {
        @NotNull
        private String topic;

        @NotNull
        @Min(value = 3, message = "커리큘럼은 최소 3단계입니다.")
        @Max(value = 6, message = "커리큘럼은 최대 6단계입니다.")
        private int stage;
    }

    @Data
    public static class Response {
        @NotBlank
        private String curriculum;
    }

}
