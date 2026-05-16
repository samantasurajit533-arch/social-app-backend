package com.social.controller;

import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class AiCaptionController {

    private final VertexAiGeminiChatModel chatModel;

    @Autowired
    public AiCaptionController(VertexAiGeminiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Updated to return a proper JSON entity instead of a raw text string.
     */
    @GetMapping(value = "/generate-caption", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateCaption(
            @RequestParam String keywords,
            @RequestHeader(value = "Authorization", required = false) String token) {

        String systemPrompt = "You are a professional social media manager. " +
                "Write a highly creative, viral post caption with 5 trending hashtags based on these keywords: " + keywords;

        String aiResponse = chatModel.call(systemPrompt);

        return ResponseEntity.ok(Map.of("caption", aiResponse));
    }

}
