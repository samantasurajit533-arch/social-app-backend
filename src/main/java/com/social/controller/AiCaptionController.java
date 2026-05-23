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
            // 3. Graceful failure reporting if GCP or Vertex credentials fail
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "AI service generation failed: " + e.getMessage()));
        }
    }
}

