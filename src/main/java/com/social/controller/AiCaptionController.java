package com.social.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin("*")
public class AiCaptionController {

    private final ChatClient chatClient;

    public AiCaptionController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping(value = "/generate-caption", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateCaption(@RequestParam(required = false) String keywords) {

        // 1. Validation fallback
        if (keywords == null || keywords.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Keywords parameter cannot be empty"));
        }

        String prompt = "You are a social media expert. " +
                "Write a viral Instagram caption with 5 hashtags for: " + keywords;

        try {
            // 2. Dynamic execution
            String response = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            return ResponseEntity.ok(Map.of(
                    "caption", response != null ? response : "No response from AI"
            ));

        } catch (Exception e) {
            // CRITICAL: Print full log stack trace to Render Dashboard console logs
            System.err.println("--- AI CAPTION GENERATION FAILED ---");
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "AI service generation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testAi() {
        // Collect server environment details securely for debugging
        String apiKeyEnv = System.getenv("SPRING_AI_GOOGLE_GENAI_API_KEY");
        String gcpIdEnv = System.getenv("gcp_id");

        System.out.println("--- DEBUGGING AI STUDIO AUTH ---");
        System.out.println("Is API Key found in Render Environment?: " + (apiKeyEnv != null));
        System.out.println("API Key character length: " + (apiKeyEnv != null ? apiKeyEnv.length() : 0));
        System.out.println("Is legacy gcp_id still active?: " + (gcpIdEnv != null));

        try {
            String response = chatClient
                    .prompt()
                    .user("Say hello")
                    .call()
                    .content();
            return ResponseEntity.ok(Map.of("response", response));
        } catch (Exception e) {
            // Log full root error to server console
            System.err.println("--- TEST AI ENDPOINT EXCEPTION ---");
            e.printStackTrace();

            return ResponseEntity.status(500)
                    .body(Map.of(
                            "error", e.getMessage() != null ? e.getMessage() : "Null message",
                            "exception_class", e.getClass().getName(),
                            "cause", e.getCause() != null ? e.getCause().toString() : "No nested cause details available"
                    ));
        }
    }
}
