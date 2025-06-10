package aico.backend.quiz;

import aico.backend.global.security.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class AmazonS3Controller {

    private final AwsS3Service awsS3Service;

    // 파일 업로드 및 퀴즈 생성
    @PostMapping
    public ResponseEntity<String> uploadFileAndQuiz(MultipartFile multipartFile) throws JsonProcessingException {
        return ResponseEntity.ok((awsS3Service.uploadFileAndQuiz(multipartFile)));
    }

    // 오답 노트 저장
    @PostMapping("/save")
    public ResponseEntity<String> saveQuizToWrongAnswerNote(@RequestBody QuizSaveRequest request,
                                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        awsS3Service.saveQuizToWrongNote(request, userDetails);
        return ResponseEntity.ok("퀴즈가 오답노트에 저장되었습니다.");
    }

    // 오답 노트 조회
    @GetMapping("/wrong-notes")
    public ResponseEntity<List<WrongAnswerNoteResponse>> getWrongAnswerNotes(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(awsS3Service.getWrongAnswerNotes(userDetails));
    }


    // 특정 퀴즈 삭제
    @DeleteMapping("/{quizId}")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long quizId,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        awsS3Service.deleteQuiz(quizId, userDetails);
        return ResponseEntity.ok("퀴즈가 삭제되었습니다.");
    }
}
