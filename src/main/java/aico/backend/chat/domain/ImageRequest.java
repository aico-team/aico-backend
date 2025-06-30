package aico.backend.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ImageRequest {
    private String model;
    private List<Map<String, Object>> input;
}
