package aico.backend.studytime.controller;

import aico.backend.studytime.domain.StudyTime;
import aico.backend.studytime.service.StudyTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/study-time")
@RequiredArgsConstructor
public class StudyTimeController {
    private final StudyTimeService service;

    @PostMapping
    public ResponseEntity<Void> saveTime(@RequestParam Long userId, @RequestParam int totalSeconds) {
        service.saveStudyTime(userId, totalSeconds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/today")
    public ResponseEntity<Integer> getToday(@RequestParam Long userId) {
        return ResponseEntity.ok(service.getTodayStudyTime(userId));
    }

    @GetMapping("/streak")
    public ResponseEntity<Integer> getStreak(@RequestParam Long userId) {
        return ResponseEntity.ok(service.getStreak(userId));
    }

    @GetMapping("/daily")
    public ResponseEntity<List<StudyTime>> getDaily(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(service.getDailyTimes(userId, start, end));
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<StudyTime>> getWeekly(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.getWeeklyTimes(userId, date));
    }
}
