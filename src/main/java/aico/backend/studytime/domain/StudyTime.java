package aico.backend.studytime.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "study_time")

public class StudyTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private LocalDate date;

    private int studySeconds;

    // Getters and Setters
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public int getStudySeconds() { return studySeconds; }
    public void setStudySeconds(int studySeconds) { this.studySeconds = studySeconds; }
}
