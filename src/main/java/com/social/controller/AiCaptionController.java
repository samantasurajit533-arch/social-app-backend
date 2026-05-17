package com.social.controller;

import org.springframework.ai.google.genai.GoogleGenAiChatModel; // আপডেটেড ইমপোর্ট
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class AiCaptionController {

    private final GoogleGenAiChatModel chatModel; // আপডেটেড টাইপ

    @Autowired
    public AiCaptionController(GoogleGenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Generates a viral social media caption based on the provided keywords.
     */
    @GetMapping(value = "/generate-caption", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateCaption(
            @RequestParam String keywords,
            @RequestHeader(value = "Authorization", required = false) String token) {

        String systemPrompt = "You are a professional social media manager. " +
                "Write a highly creative, viral post caption with 5 trending hashtags based on these keywords: " + keywords;

        // নতুন মডেল ক্লায়েন্ট দিয়ে গুগল এপিআই কল করা হচ্ছে
        String aiResponse = chatModel.call(systemPrompt);

        // JSON ফরম্যাটে রেসপন্স রিটার্ন {"caption": "..."}
        return ResponseEntity.ok(Map.of("caption", aiResponse));
    }
}

