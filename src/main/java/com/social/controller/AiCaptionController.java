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

    //  Shared Groq API caller
    private String callGroq(String prompt, int maxTokens) throws Exception {
        String apiKey = System.getenv("GROQ_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("GROQ_API_KEY not configured");
        }

        String requestBody = String.format("""
            {
              "model": "llama-3.3-70b-versatile",
              "messages": [{"role": "user", "content": "%s"}],
              "max_tokens": %d
            }
            """, prompt.replace("\"", "\\\"").replace("\n", "\\n"), maxTokens);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Groq error: " + response.body());
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        return root.path("choices").get(0)
                .path("message").path("content").asText().trim();
    }

    //  1. Generate Caption
    @GetMapping(value = "/generate-caption", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateCaption(
            @RequestParam(required = false) String keywords) {

        if (keywords == null || keywords.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Keywords cannot be empty"));
        }

        try {
            String prompt = "You are a social media expert. " +
                    "Write a viral Instagram caption with 5 relevant hashtags for: " + keywords;

            String caption = callGroq(prompt, 300)
                    .trim().replaceAll("^\"|\"$", "");

            return ResponseEntity.ok(Map.of("caption", caption));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed: " + e.getMessage()));
        }
    }

    // 2. Toxic Comment Detection
    @PostMapping(value = "/check-toxic", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkToxic(
            @RequestBody Map<String, String> body) {

        String comment = body.get("comment");

        if (comment == null || comment.trim().isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "toxic", false,
                    "message", "Empty comment"
            ));
        }

        try {
            String prompt = "You are a strict content moderator for a social media platform. " +
                    "Analyze this comment and check if it is toxic, hateful, abusive, " +
                    "offensive, violent, sexual, or inappropriate in ANY language " +
                    "including English, Bengali, Hindi, Spanish, Arabic, etc. " +
                    "Reply with ONLY one word: 'TOXIC' or 'SAFE'. " +
                    "Comment to analyze: " + comment;

            String result = callGroq(prompt, 10).toUpperCase().trim();
            boolean isToxic = result.contains("TOXIC");

            if (isToxic) {
                return ResponseEntity.ok(Map.of(
                        "toxic", true,
                        "message", "⚠️ Your comment contains inappropriate content. Please keep the community respectful!"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "toxic", false,
                        "message", "Comment is safe"
                ));
            } //

        } catch (Exception e) {
            //  If AI check fails, allow comment — don't block users
            return ResponseEntity.ok(Map.of(
                    "toxic", false,
                    "message", "Check skipped"
            ));
        }
    }
}