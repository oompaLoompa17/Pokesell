package com.example.Pokemon_TCG_TEST.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CardGraderService {
    @Value("${ximilar.api.token}")
    private String ximilarApiToken;
    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<?> gradeCard(MultipartFile frontImage, MultipartFile backImage) {
        try {
            String url = "https://api.ximilar.com/card-grader/v2/grade";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // JSON, not multipart
            headers.set("Authorization", "Token " + ximilarApiToken);

            // Convert images to base64
            String frontBase64 = Base64.getEncoder().encodeToString(frontImage.getBytes());
            String backBase64 = Base64.getEncoder().encodeToString(backImage.getBytes());

            // Build JSON body
            Map<String, Object> body = new HashMap<>();
            body.put("records", Arrays.asList(
                Map.of("_base64", "data:image/jpeg;base64," + frontBase64, "is_front", true),
                Map.of("_base64", "data:image/jpeg;base64," + backBase64, "is_front", false)
            ));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            System.out.println("Ximilar Response: " + response.getBody()); // Debug
            return response;
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to process image files: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Failed to grade card: " + e.getMessage()));
        }
    }
}