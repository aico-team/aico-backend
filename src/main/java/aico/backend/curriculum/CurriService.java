package aico.backend.curriculum;

import aico.backend.global.config.GptConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CurriService {
    private final GptConfig gptConfig;
    private final RestClient restClient;
    private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";

    public ResponseEntity<String> chatGptService(CurriDto.Request request){
        log.info("gpt 키: {}", gptConfig.getSecretKey());

        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", generatePrompt(request.getTopic(), request.getStage())
                        )
                )
        );

        log.info("gpt 키: {}", gptConfig.getSecretKey());

        return restClient.post()
                .uri(GPT_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + gptConfig.getSecretKey())
                .body(body)
                .retrieve()
                .toEntity(String.class);
    }
//    public String makeCurri(CurriDto.Request request) {
//        String prompt = generatePrompt(request.getSubject(), request.getStage());
//
//    }

    public String generatePrompt(String subject, int stage) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("나는 공부를 체계적으로 하고싶은 학생이야. 너는 나의 전담 스터디 코치 역할이야."
                + "너는 내가 공부하고 싶은 주제와, 그 주제를 효율적이고도 전문적으로 알 수 있게끔 "
                + "단계별 커리큘럼을 짜주어야해. 각 단계는 구체적이고 직관적인 한 문장이어야해. 내가 공부하고싶은 주제: \"");
        prompt.append(subject);
        prompt.append("\", 커리큘럼: ");
        prompt.append(stage);
        prompt.append("단계");
        prompt.append("답변은 커리큘럼 내용만 말해.");
        return prompt.toString();
    }

}
