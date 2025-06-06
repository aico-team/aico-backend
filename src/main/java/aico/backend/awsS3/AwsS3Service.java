package aico.backend.awsS3;

import aico.backend.chat.domain.ImageRequest;
import aico.backend.global.config.GptConfig;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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

    public String uploadFileAndQuiz(MultipartFile multipartFile) throws JsonProcessingException {

        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String fileName = createFileName(multipartFile.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try(InputStream inputStream = multipartFile.getInputStream()){
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        }

        String imageUrl = getFileUrl(fileName);
        return quizByImage(imageUrl);
    }

    public String quizByImage(String imageUrl) throws JsonProcessingException {
        log.info("✅ 이미지 url: {}", imageUrl);

        String prompt = """
               이 파일을 바탕으로, 학습자가 잘 이해했는 지를 판별할 수 있는 퀴즈 3개를 만들어.
               응답은 반드시 아래 JSON 형식의 문자열로만 답변해줘. 다른 설명은 포함하지 마.
               {"quiz1" : "퀴즈1 내용", "ans1" : "퀴즈1 답", ...}
               """;

        ImageRequest requestBody = createImageRequest(imageUrl, prompt);

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

    private ImageRequest createImageRequest(String imageUrl, String prompt) {
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

}
