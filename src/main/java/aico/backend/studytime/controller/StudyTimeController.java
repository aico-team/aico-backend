package aico.backend.studytime.controller;

import aico.backend.studytime.domain.StudyTime;
import aico.backend.studytime.service.StudyTimeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/study-time")

public class StudyTimeController {
    private final StudyTimeService service;

    public StudyTimeController(StudyTimeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> saveTime(@RequestParam String userId, @RequestParam int totalSeconds) {
        service.saveStudyTime(userId, totalSeconds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/today")
    public ResponseEntity<Integer> getToday(@RequestParam String userId) {
        return ResponseEntity.ok(service.getTodayStudyTime(userId));
    }

    @GetMapping("/streak")
    public ResponseEntity<Integer> getStreak(@RequestParam String userId) {
        return ResponseEntity.ok(service.getStreak(userId));
    }

    @GetMapping("/daily")
    public ResponseEntity<List<StudyTime>> getDaily(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.getDaily(userId, start, end));
    }
}
