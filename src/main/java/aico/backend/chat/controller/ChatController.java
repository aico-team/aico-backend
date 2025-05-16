package aico.backend.chat.controller;

import aico.backend.chat.service.ChatService;
import aico.backend.chat.domain.ChatRequest;
import aico.backend.global.security.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody ChatRequest request,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) throws JsonProcessingException {
        String reply = chatService.sendMessage(request, userDetails);
        return ResponseEntity.ok(reply);
    }

}
