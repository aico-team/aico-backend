package aico.backend.chat;

import aico.backend.global.config.GptConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
    private final GptConfig gptConfig;
    private final RestClient restClient;

}
