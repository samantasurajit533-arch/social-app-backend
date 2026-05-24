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

    @GetMapping(value = "/generate-caption", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateCaption(
            @RequestParam(required = false) String keywords) {

        if (keywords == null || keywords.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Keywords cannot be empty"));
        }

        try {
            String apiKey = System.getenv("GROQ_API_KEY");

            if (apiKey == null || apiKey.trim().isEmpty()) {
                return ResponseEntity.status(500)
                        .body(Map.of("error", "GROQ_API_KEY not configured in environment"));
            }

            String url = "https://api.groq.com/openai/v1/chat/completions";

            String prompt = "You are a social media expert. Write a viral Instagram caption with 5 relevant hashtags for: " + keywords;

            String requestBody = String.format("""
                {
                  "model": "llama-3.3-70b-versatile",
                  "messages": [
                    {
                      "role": "user",
                      "content": "%s"
                    }
                  ],
                  "max_tokens": 300
                }
                """, prompt.replace("\"", "\\\"").replace("\n", "\\n"));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return ResponseEntity.status(500)
                        .body(Map.of("error", "Groq API error: " + response.body()));
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            String caption = root
                    .path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            if (caption == null || caption.isEmpty()) {
                return ResponseEntity.status(500)
                        .body(Map.of("error", "Empty response from Groq"));
            }

            return ResponseEntity.ok(Map.of("caption", caption));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed: " + e.getMessage()));
        }
    }
}