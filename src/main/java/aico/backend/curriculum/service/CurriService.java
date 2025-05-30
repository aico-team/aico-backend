package aico.backend.curriculum.service;

import aico.backend.curriculum.dto.CompletionDto;
import aico.backend.curriculum.dto.CurriDto;
import aico.backend.curriculum.domain.Curriculum;
import aico.backend.curriculum.domain.CurriculumStep;
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

            curriculumMap.put(stage, new CurriculumStep(description, false, null));
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

    @Transactional
    public CurriDto.Response confirmCurriculum(CurriDto.Request request, UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        Map<String, CurriculumStep> curriculumMap = parseContentToMap(request.getContent());

        Curriculum curriculum = Curriculum.builder()
                .topic(request.getTopic())
                .user(user)
                .curriculumMap(curriculumMap)
                .progress(0.0)
                .build();

        Curriculum savedCurri = curriRepository.save(curriculum);
        return new CurriDto.Response(savedCurri.getId(), savedCurri.getTopic(), curriculumMap);
    }

    public List<CurriDto.Response> getCurriList(UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        List<Curriculum> curries = curriRepository.findByUser(user);
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

    @Transactional
    public Double changeCompletion(CompletionDto request, UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        Curriculum curriculum = curriRepository.findById(request.getId()).orElseThrow(
                () -> new CurriNotFoundException("해당 커리큘럼이 없습니다.")
        );

        if (!curriculum.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 커리큘럼에 접근 권한이 없습니다.");
        }

        curriculum.changeCompletion(request.getStage(), request.getCompleted());
        int completedStages = curriRepository.countCompletedStagesById(request.getId());
        double percent = (double) completedStages / curriculum.getCurriculumMap().size();
        return curriculum.changeProgress(Math.round(percent * 1000) / 10.0);
    }

    public Double getProgress(Long id, UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        Curriculum curriculum = curriRepository.findById(id).orElseThrow(
                () -> new CurriNotFoundException("해당 커리큘럼이 없습니다.")
        );

        if (!curriculum.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("해당 커리큘럼에 접근 권한이 없습니다.");
        }

        return curriculum.getProgress();
    }

}
