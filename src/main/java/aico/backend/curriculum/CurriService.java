package aico.backend.curriculum;

import aico.backend.global.config.GptConfig;
import aico.backend.global.security.UserDetailsImpl;
import aico.backend.user.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CurriService {
    private final GptConfig gptConfig;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final CurriRepository curriRepository;
    private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";


    public String getGptCurriculum(String topic, int stage) throws JsonProcessingException {
        Map<String, Object> body = Map.of(
                "model", gptConfig.getModel(),
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", generatePrompt(topic, stage)
                        )
                )
        );

        ResponseEntity<String> response = restClient.post()
                .uri(GPT_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + gptConfig.getSecretKey())
                .body(body)
                .retrieve()
                .toEntity(String.class);

        return parseGptResponse(response.getBody());
    }

    public Map<String, CurriculumStep> parseContentToMap(String response) {
        Map<String, CurriculumStep> curriculumMap = new HashMap<>();
        String[] steps = response.split("\n");

        for (String step : steps) {
            String[] split = step.split(":", 2);
            String stage = split[0].trim();
            String description = split[1].trim();

            curriculumMap.put(stage, new CurriculumStep(description, false));
        }

        return curriculumMap;
    }

    public String parseGptResponse(String response) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(response);
        return rootNode.get("choices")
                       .get(0)
                       .get("message")
                       .get("content")
                       .asText();
    }

    public String generatePrompt(String subject, int stage) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("나는 공부를 체계적으로 하고싶은 학생이야. 너는 나의 전담 스터디 코치 역할이야. "
                + "내가 공부하고싶은 주제를, 효율적이고 전문적으로 알 수 있게 단계별 커리큘럼을 짜줘. "
                + "단계는 각각 '구체적이고, 직접 실력 향상에 필요한 핵심 내용'만 포함해야해. "
                + "절대 금지: 주제의 역사, 주변 이야기, 기초 개념 나열, 이론적 배경. "
                + "너는 프로 강사처럼 생각해야 해. "
                + "내가 공부하고싶은 주제: \"");
        prompt.append(subject);
        prompt.append("\", 단계 수: ");
        prompt.append(stage);
        prompt.append("답변은 \"n: 내용\" 형식이며, 여기서 n은 단계를 나타내. 구어체 생략형 표현으로 내용을 생성하고, 커리큘럼 내용만 말해.");
        return prompt.toString();
    }

    public CurriDto.Response confirmCurriculum(CurriDto.Request request, UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        Map<String, CurriculumStep> curriculumMap = parseContentToMap(request.getContent());

        Curriculum curriculum = Curriculum.builder()
                .topic(request.getTopic())
                .user(user)
                .curriculum(curriculumMap)
                .build();

        curriRepository.save(curriculum);
        return new CurriDto.Response(curriculumMap);
    }
}
