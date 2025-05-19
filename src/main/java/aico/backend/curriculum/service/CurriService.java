package aico.backend.curriculum.service;

import aico.backend.chat.domain.ChatMessage;
import aico.backend.curriculum.dto.CurriDto;
import aico.backend.curriculum.domain.Curriculum;
import aico.backend.curriculum.domain.CurriculumStep;
import aico.backend.curriculum.dto.RecommendDto;
import aico.backend.curriculum.repository.CurriRepository;
import aico.backend.global.config.GptConfig;
import aico.backend.global.exception.curriculum.CurriNotFoundException;
import aico.backend.global.security.UserDetailsImpl;
import aico.backend.user.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CurriService {
    private final GptConfig gptConfig;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final CurriRepository curriRepository;


    public String getGptCurriculum(String topic, int stage) throws JsonProcessingException {
        List<ChatMessage> message = new ArrayList<>();
        message.add(new ChatMessage("user", generatePrompt(topic, stage)));
        return sendApiRequest(message);
    }

    private String sendApiRequest(List<ChatMessage> message) throws JsonProcessingException {
        Map<String, Object> body = new HashMap<>();
        body.put("model", gptConfig.getModel());
        body.put("messages", message);

        ResponseEntity<String> response = restClient.post()
                .uri(gptConfig.getUri())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + gptConfig.getSecretKey())
                .body(body)
                .retrieve()
                .toEntity(String.class);

        JsonNode rootNode = objectMapper.readTree(response.getBody());
        return rootNode.get("choices")
                .get(0)
                .get("message")
                .get("content")
                .asText();
    }

    public Map<String, CurriculumStep> parseContentToMap(String content) {
        Map<String, CurriculumStep> curriculumMap = new HashMap<>();
        String[] steps = content.split("\n");

        for (String step : steps) {
            String[] split = step.split(":", 2);
            String stage = split[0].trim();
            String description = split[1].trim();

            curriculumMap.put(stage, new CurriculumStep(description, false, ""));
        }

        return curriculumMap;
    }

    public String generatePrompt(String subject, int stage) {
        return "나는 공부를 체계적으로 하고싶은 학생이야. 너는 나의 전담 스터디 코치 역할이야. "
                + "내가 공부하고싶은 주제를, 효율적이고 전문적으로 알 수 있게 단계별 커리큘럼을 짜줘. "
                + "단계는 각각 '구체적이고, 직접 실력 향상에 필요한 핵심 내용'만 포함해야해. "
                + "절대 금지: 주제의 역사, 주변 이야기, 기초 개념 나열, 이론적 배경. "
                + "너는 프로 강사처럼 생각해야 해. "
                + "내가 공부하고싶은 주제: \"" +
                subject +
                "\", 단계 수: " +
                stage +
                "답변은 \"n: 내용\" 형식이며, 여기서 n은 단계를 나타내. 구어체 생략형 표현으로 내용을 생성하고, 커리큘럼 내용만 말해.";
    }

    @Transactional
    public CurriDto.Response confirmCurriculum(CurriDto.Request request, UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        Map<String, CurriculumStep> curriculumMap = parseContentToMap(request.getContent());

        Curriculum curriculum = new Curriculum(request.getTopic(), user, curriculumMap, new HashMap<>());

        Curriculum savedCurri = curriRepository.save(curriculum);
        return new CurriDto.Response(savedCurri.getId(), savedCurri.getTopic(), curriculumMap);
    }

    public List<CurriDto.Response> getCurriList(UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        List<Curriculum> curries = curriRepository.findByUser(user);
        if(curries.isEmpty()) {
            throw new CurriNotFoundException("해당 사용자의 커리큘럼이 없습니다.");
        }
        List<CurriDto.Response> response = new ArrayList<>();
        for(Curriculum curri : curries) {
            response.add(new CurriDto.Response(curri.getId(), curri.getTopic(), curri.getCurriculumMap()));
        }

        return response;
    }

    @Transactional
    public void deleteCurriculum(Long id, UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        Curriculum curriculum = curriRepository.findById(id)
                .orElseThrow(() -> new CurriNotFoundException("존재하지않는 커리큘럼입니다."));

        if (!curriculum.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        curriRepository.delete(curriculum);
    }

    public String recommendStudy(UserDetailsImpl userDetails, RecommendDto.Request recommendDto) throws JsonProcessingException {
        Curriculum curriculum = curriRepository.findById(recommendDto.getId())
                .orElseThrow(() -> new CurriNotFoundException(""));

        User user = userDetails.getUser();
        if (!curriculum.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("이 커리큘럼에 접근할 수 없습니다.");
        }

        String stage = recommendDto.getStage();
        String stageDescription = curriculum.getStepDescription(stage);

        String prompt = "다음 내용을 공부하기에 도움이 되는 웹사이트 링크와 제목을 3개 추천해줘." +
                "내용: " +
                stageDescription +
                "조건: 1. 각 추천은 웹사이트 제목과 링크로 구성된다. " +
                "2. [제목] - 링크 주소 형식으로만 대답하라." +
                "3. 다른 추천 사이에는 빈 줄로 구분하라." +
                "4. 제목, 링크만 말하라.";

        List<ChatMessage> message = new ArrayList<>();
        message.add(new ChatMessage("user", prompt));
        String response = sendApiRequest(message);

        curriculum.getRecommendations().put(stage, response);
        curriRepository.save(curriculum);

        return response;
    }
}
