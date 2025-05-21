package aico.backend.studytime.service;

import aico.backend.studytime.domain.StudyTime;
import aico.backend.studytime.repository.StudyTimeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudyTimeService {
    private final StudyTimeRepository repo;

    public StudyTimeService(StudyTimeRepository repo) {
        this.repo = repo;
    }

    public void saveStudyTime(String userId, int totalSeconds) {
        LocalDate today = LocalDate.now();
        StudyTime record = repo.findByUserIdAndDate(userId, today)
                .orElse(new StudyTime());

        record.setUserId(userId);
        record.setDate(today);
        record.setStudySeconds(totalSeconds);

        repo.save(record);
    }

    public int getTodayStudyTime(String userId) {
        return repo.findByUserIdAndDate(userId, LocalDate.now())
                .map(StudyTime::getStudySeconds)
                .orElse(0);
    }

    public int getStreak(String userId) {
        List<StudyTime> records = repo.findByUserIdOrderByDateDesc(userId);
        int streak = 0;
        LocalDate today = LocalDate.now();

        for (StudyTime r : records) {
            if (!r.getDate().equals(today)) break;
            if (r.getStudySeconds() < 1800) break;
            streak++;
            today = today.minusDays(1);
        }

        return streak;
    }

    public List<StudyTime> getDaily(String userId, LocalDate start, LocalDate end) {
        return repo.findByUserIdAndDateBetween(userId, start, end);
    }
}
