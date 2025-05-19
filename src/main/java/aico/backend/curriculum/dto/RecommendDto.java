package aico.backend.curriculum.dto;

import lombok.Data;

public class RecommendDto {

    @Data
    public static class Request {
        private Long id;
        private String stage;
    }

    @Data
    public static class Response {
        private String title;
        private String url;
    }

}