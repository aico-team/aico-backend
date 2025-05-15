package aico.backend.chat.service;

import aico.backend.chat.domain.ChatMessage;
import aico.backend.chat.domain.ChatRequest;
import aico.backend.global.config.GptConfig;
import aico.backend.global.security.UserDetailsImpl;
import aico.backend.user.domain.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
    private final GptConfig gptConfig;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    public String sendMessage(ChatRequest request, UserDetailsImpl userDetails) throws JsonProcessingException {
        User user = userDetails.getUser();
        String userId = String.valueOf(user.getId());
        Cache cache = cacheManager.getCache("chatCache");
        List<ChatMessage> messages = cache.get(userId, List.class);

        // 이전 대화가 없다면 system 설정
        if (messages == null) {
            messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "너는 AICO라는 서비스의 챗봇이야. 답변은 구어체 존댓말로 명확히 알려줘."));
        }

        // user 질문 추가
        messages.add(new ChatMessage("user", request.getMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("model", gptConfig.getModel());
        body.put("messages", messages);

        ResponseEntity<String> response = restClient.post()
                .uri(gptConfig.getUri())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + gptConfig.getSecretKey())
                .body(body)
                .retrieve()
                .toEntity(String.class);

        JsonNode rootNode = objectMapper.readTree(response.getBody());

        String reply = rootNode.get("choices")
                               .get(0)
                               .get("message")
                               .get("content")
                               .asText();

        int usedToken = rootNode.get("usage")
                .get("total_tokens")
                .asInt();

        if(usedToken > 2048) {
            messages.clear();
            messages.add(new ChatMessage("system", "너는 AICO라는 서비스의 챗봇이야. 답변은 구어체 존댓말로 명확히 알려줘."));
            messages.add(new ChatMessage("user", request.getMessage()));
        }

        messages.add(new ChatMessage("assistant", reply));
        cache.put(userId, messages);
        return reply;
    }
}
