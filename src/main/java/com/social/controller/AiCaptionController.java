package com.social.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin("*")
public class AiCaptionController {

    private final ChatClient chatClient;

    // Inject the ChatClient bean directly from AiConfig
    public AiCaptionController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping(value = "/generate-caption", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateCaption(@RequestParam String keywords) {

        String prompt = "You are a social media expert. " +
                "Write a viral Instagram caption with 5 hashtags for: " + keywords;

        String response = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

        return ResponseEntity.ok(Map.of(
                "caption",
                response != null ? response : "No response from AI"
        ));
    }
}
