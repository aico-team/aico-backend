package aico.backend.curriculum;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Transactional
@RequiredArgsConstructor
public class CurriService {
    private final RestClient restClient;

    public String makeCurri(CurriDto.Request request) {
        String prompt = generatePrompt(request.getSubject(), request.getStage());

    }

    public String generatePrompt(String subject, int stage) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("나는 공부를 체계적으로 하고싶은 학생이야. 너는 나의 전담 스터디 코치 역할이야."
                + "너는 내가 공부하고 싶은 주제와, 그 주제를 효율적이고도 전문적으로 알 수 있게끔 "
                + "커리큘럼을 짜주어야해. \n 내가 공부하고싶은 주제는 \"");
        prompt.append(subject);
        prompt.append("\" 이고, 커리큘럼은 총");
        prompt.append(stage);
        prompt.append("단계로 짜줘.");
        return prompt.toString();
    }

}
