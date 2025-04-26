package aico.backend.curriculum.controller;

import aico.backend.curriculum.dto.CurriDto;
import aico.backend.curriculum.service.CurriService;
import aico.backend.global.security.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/curri")
@RequiredArgsConstructor
public class CurriController {
    private final CurriService curriService;

    @GetMapping
    public ResponseEntity<String> makeCurri(@RequestParam String topic, @RequestParam int stage) throws JsonProcessingException {
        String curriculum = curriService.getGptCurriculum(topic, stage);
        return ResponseEntity.ok(curriculum);
    }

    @PostMapping("/confirm")
    public ResponseEntity<CurriDto.Response> confirmCurri(@RequestBody CurriDto.Request request,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CurriDto.Response curriculumMap = curriService.confirmCurriculum(request, userDetails);
        return ResponseEntity.ok(curriculumMap);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurri(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        curriService.deleteCurriculum(id, userDetails);
        return ResponseEntity.noContent().build();
    }
}
