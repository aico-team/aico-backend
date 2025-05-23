package aico.backend.curriculum.controller;

import aico.backend.curriculum.dto.CompletionDto;
import aico.backend.curriculum.dto.CurriDto;
import aico.backend.curriculum.service.CurriService;
import aico.backend.global.security.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/curri")
@RequiredArgsConstructor
@Slf4j
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

    @GetMapping("/list")
    public ResponseEntity<List<CurriDto.Response>> getCurriList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<CurriDto.Response> curriList = curriService.getCurriList(userDetails);
        return ResponseEntity.ok(curriList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurri(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        curriService.deleteCurriculum(id, userDetails);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/complete")
    public ResponseEntity<Double> changeCompletion(@RequestBody CompletionDto request,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Double percent = curriService.changeCompletion(request, userDetails);
        return ResponseEntity.ok(percent);
    }

    @GetMapping("/complete/{id}")
    public ResponseEntity<Double> changeCompletion(@PathVariable Long id,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Double percent = curriService.getProgress(id, userDetails);
        return ResponseEntity.ok(percent);
    }


}
