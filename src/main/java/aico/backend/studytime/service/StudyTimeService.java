package aico.backend.studytime.service;

import aico.backend.studytime.domain.StudyTime;
import aico.backend.studytime.repository.StudyTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyTimeService {
    private final StudyTimeRepository repo;

    public void saveStudyTime(Long userId, int totalSeconds) {
        LocalDate today = LocalDate.now();
        StudyTime record = repo.findByUserIdAndDate(userId, today)
                .orElse(new StudyTime());

        record.setUserId(userId);
        record.setDate(today);
        record.setStudySeconds(totalSeconds);

        repo.save(record);
    }

    public int getTodayStudyTime(Long userId) {
        return repo.findByUserIdAndDate(userId, LocalDate.now())
                .map(StudyTime::getStudySeconds)
                .orElse(0);
    }

    public int getStreak(Long userId) {
        List<StudyTime> records = repo.findByUserIdAndDateBeforeOrderByDateDesc(userId, LocalDate.now().plusDays(1));

        int streak = 0;
        LocalDate date = LocalDate.now();

        for (StudyTime record : records) {
            if (!record.getDate().equals(date)) break;
            if (record.getStudySeconds() < 1800) break;
            streak++;
            date = date.minusDays(1);
        }

        return streak;
    }

    public List<StudyTime> getDailyTimes(Long userId, LocalDate start, LocalDate end) {
        return repo.findByUserIdAndDateBetween(userId, start, end);
    }

    public List<StudyTime> getWeeklyTimes(Long userId, LocalDate date) {
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);
        return repo.findByUserIdAndDateBetween(userId, monday, sunday);
    }
}
