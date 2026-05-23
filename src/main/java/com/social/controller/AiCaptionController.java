package com.social.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin("*")
public class AiCaptionController {

    private static final int MAX_RETRIES = 3;
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=";

    @GetMapping(value = "/generate-caption", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateCaption(
            @RequestParam(required = false) String keywords) {

        // ✅ Step 1: Validate input
        if (keywords == null || keywords.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Keywords cannot be empty"));
        }

        // ✅ Step 2: Get API Key
        String apiKey = System.getenv("AI_API_Key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "AI_API_Key not set in environment"));
        }

        // ✅ Step 3: Call Gemini with Retry
        try {
            String caption = callGeminiWithRetry(apiKey, keywords);
            return ResponseEntity.ok(Map.of("caption", caption));

        } catch (QuotaExceededException e) {
            // 429 - Quota exceeded after all retries
            return ResponseEntity.status(429)
                    .body(Map.of("error", "AI quota exceeded. Please try again later."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed: " + e.getMessage()));
        }
    }

    // ✅ Retry Method with Exponential Backoff
    private String callGeminiWithRetry(String apiKey, String keywords) throws Exception {

        String prompt = "You are a social media expert. Write a viral Instagram caption with 5 relevant hashtags for: " + keywords;

        String requestBody = String.format("""
            {
              "contents": [{
                "parts": [{"text": "%s"}]
              }]
            }
            """, prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        HttpClient client = HttpClient.newHttpClient();
        int attempt = 0;

        while (attempt < MAX_RETRIES) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            // ✅ Success
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                return root
                        .path("candidates").get(0)
                        .path("content")
                        .path("parts").get(0)
                        .path("text").asText();
            }

            // ✅ Quota exceeded - wait and retry
            if (response.statusCode() == 429) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new QuotaExceededException("Quota exceeded after " + MAX_RETRIES + " retries");
                }

                // Exponential backoff: 2s → 4s → 8s
                long waitTime = (long) Math.pow(2, attempt) * 1000;
                System.out.println("Quota hit! Retrying in " + waitTime + "ms... (Attempt " + attempt + ")");
                Thread.sleep(waitTime);
                continue;
            }

            // ✅ Other errors - don't retry
            throw new RuntimeException("Gemini API error " + response.statusCode() + ": " + response.body());
        }

        throw new QuotaExceededException("Max retries reached");
    }

    // ✅ Custom Exception for Quota
    static class QuotaExceededException extends RuntimeException {
        public QuotaExceededException(String message) {
            super(message);
        }
    }
}