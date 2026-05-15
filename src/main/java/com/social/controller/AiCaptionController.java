package com.social.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*") // Allows your React app to connect
public class AiCaptionController {

    private final ChatModel chatModel;

    // Spring Boot automatically injects the Gemini ChatModel here
    @Autowired
    public AiCaptionController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/generate-caption")
    public String generateCaption(@RequestParam String keywords) {
        // Prompt Engineering: Instructing the AI on how to behave
        String prompt = "You are an expert social media influencer. " +
                "Write a viral, creative caption with 5 trending hashtags based on these keywords: " + keywords;

        // This calls the Gemini API directly
        return chatModel.call(prompt);
    }
}

