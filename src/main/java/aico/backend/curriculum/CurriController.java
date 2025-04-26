package aico.backend.curriculum;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/curri")
@RequiredArgsConstructor
public class CurriController {
    private final CurriService curriService;

    @PostMapping()
    public ResponseEntity<String> makeCurri(@RequestBody CurriDto.Request request) {
        return curriService.chatGptService(request);
    }
}
