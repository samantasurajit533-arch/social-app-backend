package com.social.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.social.models.UserMood;
import com.social.repository.UserMoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/mood")
@CrossOrigin("*")
public class AiMoodController {

    @Autowired
    private UserMoodRepository userMoodRepository;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private String callGroq(String prompt, int maxTokens) throws Exception {
        String apiKey = System.getenv("GROQ_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("GROQ_API_KEY environment variable is not configured");
        }

        ObjectNode requestJson = mapper.createObjectNode();
        requestJson.put("model", "llama-3.3-70b-versatile");
        requestJson.put("max_tokens", maxTokens);

        ObjectNode responseFormat = mapper.createObjectNode();
        responseFormat.put("type", "json_object");
        requestJson.set("response_format", responseFormat);

        ObjectNode messageNode = mapper.createObjectNode();
        messageNode.put("role", "user");
        messageNode.put("content", prompt);
        requestJson.putArray("messages").add(messageNode);

        String requestBody = mapper.writeValueAsString(requestJson);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String url = "https://api.groq.com/openai/v1/chat/completions";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Groq HTTP Error: " + response.getStatusCode() + " - " + response.getBody());
        }

        JsonNode root = mapper.readTree(response.getBody());
        return root.path("choices").get(0).path("message").path("content").asText().trim();
    }

    @PostMapping(value = "/analyze", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> analyzeAndSaveMood(@RequestBody Map<String, Object> body) {
        String userId = body.getOrDefault("userId", "").toString();
        String recentComments = body.getOrDefault("recentComments", "").toString();
        String scrolledCategories = body.getOrDefault("scrolledCategories", "").toString();

        if (userId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "UserId is required"));
        }

        try {
            String prompt = "You are a psychological AI assistant for a social media shield platform. " +
                    "Analyze the user's latest social media interactions to detect their emotional state. " +
                    "User's recent comments: \"" + recentComments + "\". " +
                    "Categories scrolled: \"" + scrolledCategories + "\". " +
                    "Determine if they are SAD, ANGRY, LOVING, HAPPY, or NORMAL. " +
                    "You MUST reply ONLY with a valid JSON object matching this schema exactly: " +
                    "{\"mood\": \"SAD\" or \"ANGRY\" or \"LOVING\" or \"HAPPY\" or \"NORMAL\", " +
                    "\"blockCategories\": [\"politics\", \"news\"], " +
                    "\"boostCategories\": [\"comedy\", \"motivation\"]}";

            String aiResponse = callGroq(prompt, 250);
            JsonNode rootNode = mapper.readTree(aiResponse);

            String mood = rootNode.path("mood").asText("NORMAL").toUpperCase().trim();

            List<String> blockList = new ArrayList<>();
            rootNode.path("blockCategories").forEach(node -> blockList.add(node.asText()));

            List<String> boostList = new ArrayList<>();
            rootNode.path("boostCategories").forEach(node -> boostList.add(node.asText()));

            UserMood userMood = new UserMood();
            userMood.setUserId(userId);
            userMood.setDetectedMood(mood);
            userMood.setBlockCategories(String.join(",", blockList));
            userMood.setBoostCategories(String.join(",", boostList));
            userMood.setLastUpdated(LocalDateTime.now());

            userMoodRepository.save(userMood);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "mood", mood,
                    "blockCategories", blockList,
                    "boostCategories", boostList
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "mood", "NORMAL",
                    "error", "Exception in analysis: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<Map<String, Object>> getMoodStatus(@PathVariable String userId) {
        return userMoodRepository.findById(userId)
                .map(mood -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "mood", mood.getDetectedMood().toUpperCase().trim(),
                        "blockCategories", List.of(mood.getBlockCategories().split(","))
                )))
                .orElse(ResponseEntity.ok(Map.of("success", true, "mood", "NORMAL", "blockCategories", List.of())));
    }
}
