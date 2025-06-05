package aico.backend.studytime.domain;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseDto {
    LocalDate date;
    int minutes;

    public ResponseDto(LocalDate date, int minutes) {
        this.date = date;
        this.minutes = minutes;
    }

    public static List<ResponseDto> from(List<StudyTime> records) {
        List<ResponseDto> response = new ArrayList<>();
        for(StudyTime record : records) {
            response.add(new ResponseDto(record.getDate(), record.getStudySeconds()/60));
        }
        return response;
    }
}
