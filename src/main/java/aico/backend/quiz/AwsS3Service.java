package aico.backend.quiz;

import aico.backend.chat.domain.ImageRequest;
import aico.backend.global.config.GptConfig;
import aico.backend.global.security.UserDetailsImpl;
import aico.backend.user.domain.User;
import aico.backend.user.repository.UserRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsS3Service {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;
    private final RestClient restClient;
    private final GptConfig gptConfig;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    public String uploadFileAndQuiz(MultipartFile multipartFile) throws JsonProcessingException {

        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String fileName = createFileName(multipartFile.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        }

        String imageUrl = getFileUrl(fileName);
        String quizResult = quizByImage(imageUrl);
        log.info("🥃🥃퀴즈 결과물: {}", quizResult);

        // 응답에 파일 정보 추가
        ObjectNode responseNode = objectMapper.createObjectNode();
        JsonNode quizNode = objectMapper.readTree(quizResult);

        // 퀴즈 정보 복사
        quizNode.fieldNames().forEachRemaining(fieldName -> {
            responseNode.set(fieldName, quizNode.get(fieldName));
        });

        // 파일 정보 추가
        responseNode.put("imageUrl", imageUrl);
        responseNode.put("fileName", fileName);
        responseNode.put("originalFileName", multipartFile.getOriginalFilename());

        return objectMapper.writeValueAsString(responseNode);
    }

    public String quizByImage(String imageUrl) throws JsonProcessingException {
        log.info("✅ 이미지 url: {}", imageUrl);

        String prompt = """
               이 파일을 바탕으로, 학습자가 잘 이해했는 지를 판별할 수 있는 퀴즈 3개를 만들어.
               응답은 반드시 아래 형식의 문자열로만 답변해줘. 절대 다른 설명은 포함하지 마.
               {"quiz1" : "퀴즈1 내용", "ans1" : "퀴즈1 답", ...}
               """;

        ImageRequest requestBody = createImageRequestBody(imageUrl, prompt);

        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(requestBody);
        log.info("✅ 요청 json: {}", requestJson);

        ResponseEntity<String> response = restClient.post()
                .uri("https://api.openai.com/v1/responses")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + gptConfig.getSecretKey())
                .body(requestBody)
                .retrieve()
                .toEntity(String.class);

        return objectMapper.readTree(response.getBody())
                .get("output")
                .get(0)
                .get("content")
                .get(0)
                .get("text")
                .asText();
    }

    // 특정 퀴즈를 오답 노트에 저장
    public void saveQuizToWrongNote(QuizSaveRequest request, UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(EntityNotFoundException::new);

        Quiz quiz = Quiz.builder()
                .imageUrl(request.getImageUrl())
                .fileName(request.getFileName())
                .originalFileName(request.getOriginalFileName())
                .user(user)
                .quiz(request.getQuiz())
                .answer(request.getAnswer())
                .build();

        quizRepository.save(quiz);
    }

    // 오답 노트 조회
    @Transactional(readOnly = true)
    public List<WrongAnswerNoteResponse> getWrongAnswerNotes(UserDetailsImpl userDetails) {
        List<Quiz> quizzes = quizRepository.findByUserIdOrderByImageUrl(userDetails.getId());

        Map<String, List<Quiz>> groupedByFile = quizzes.stream()
                .collect(Collectors.groupingBy(Quiz::getFileName));

        return groupedByFile.entrySet().stream()
                .map(entry -> {
                    List<Quiz> fileQuizzes = entry.getValue();
                    Quiz firstQuiz = fileQuizzes.get(0);

                    List<WrongAnswerNoteResponse.QuizItem> quizItems = fileQuizzes.stream()
                            .map(quiz -> WrongAnswerNoteResponse.QuizItem.builder()
                                    .id(quiz.getId())
                                    .quiz(quiz.getQuiz())
                                    .answer(quiz.getAnswer())
                                    .createdAt(quiz.getCreatedAt())
                                    .build())
                            .collect(Collectors.toList());

                    return WrongAnswerNoteResponse.builder()
                            .fileName(firstQuiz.getFileName())
                            .originalFileName(firstQuiz.getOriginalFileName())
                            .imageUrl(firstQuiz.getImageUrl())
                            .quizzes(quizItems)
                            .createdAt(firstQuiz.getCreatedAt())
                            .build();
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // 최신순 정렬
                .collect(Collectors.toList());
    }

    // 특정 퀴즈 삭제
    public void deleteQuiz(Long quizId, UserDetailsImpl userDetails) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "퀴즈를 찾을 수 없습니다."));

        if (!quiz.getUser().getId().equals(userDetails.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }

        String fileName = quiz.getFileName();
        quizRepository.deleteById(quizId);

        // 해당 파일과 관련된 다른 퀴즈가 있는지 확인
        long remainingQuizCount = quizRepository.countByUserIdAndFileName(userDetails.getId(), fileName);

        // 관련된 퀴즈가 모두 삭제되었으면 S3에서 파일도 삭제
        if (remainingQuizCount == 0) {
            deleteFileFromS3(fileName);
        }
    }

    private ImageRequest createImageRequestBody(String imageUrl, String prompt) {
        // content 배열 생성
        List<Map<String, Object>> contentList = new ArrayList<>();

        // 텍스트 content 추가
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "input_text");
        textContent.put("text", prompt);
        contentList.add(textContent);

        // 이미지 content 추가
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "input_image");
        imageContent.put("image_url", imageUrl);
        contentList.add(imageContent);

        // message 생성
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", contentList);
        messages.add(message);

        // OpenAI 요청 객체 생성
        return new ImageRequest(gptConfig.getModel(), messages);
    }

    public String getFileUrl(String fileName) {
        return amazonS3.getUrl(bucket, fileName).toString();
    }

    // 파일명을 난수화하기 위해 UUID 를 활용하여 난수를 돌린다.
    public String createFileName(String fileName){
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    //  "."의 존재 유무만 판단
    private String getFileExtension(String fileName){
        try{
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일" + fileName + ") 입니다.");
        }
    }

    // S3에서 파일 삭제 (private 메서드로 변경)
    private void deleteFileFromS3(String fileName){
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
            log.info("S3에서 파일 삭제 완료: {}", fileName);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", fileName, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다.");
        }
    }

}
