
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
import java.util.*;

@RestController
@RequestMapping("/api/ai/mood")
@CrossOrigin("*")
public class AiMoodController {

    @Autowired
    private UserMoodRepository userMoodRepository;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    // =========================
    // GROQ AI CALL
    // =========================
    private String callGroq(String prompt, int maxTokens) throws Exception {

        String apiKey = System.getenv("GROQ_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("GROQ_API_KEY not configured");
        }

        ObjectNode requestJson = mapper.createObjectNode();

        requestJson.put("model", "llama-3.3-70b-versatile");
        requestJson.put("max_tokens", maxTokens);

        ObjectNode responseFormat = mapper.createObjectNode();
        responseFormat.put("type", "json_object");

        requestJson.set("response_format", responseFormat);

        ObjectNode message = mapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);

        requestJson.putArray("messages").add(message);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity =
                new HttpEntity<>(mapper.writeValueAsString(requestJson), headers);

        String url =
                "https://api.groq.com/openai/v1/chat/completions";

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(
                    "Groq API Error : " + response.getBody()
            );
        }

        JsonNode root = mapper.readTree(response.getBody());

        return root.path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText()
                .trim();
    }

    // =========================
    // ANALYZE USER MOOD
    // =========================
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeMood(
            @RequestBody Map<String, Object> body
    ) {

        try {

            String userId =
                    body.getOrDefault("userId", "").toString();

            String recentComments =
                    body.getOrDefault("recentComments", "").toString();

            String scrolledCategories =
                    body.getOrDefault("scrolledCategories", "").toString();

            Integer watchTime =
                    Integer.parseInt(
                            body.getOrDefault("watchTime", "0").toString()
                    );

            Integer repeatViews =
                    Integer.parseInt(
                            body.getOrDefault("repeatViews", "0").toString()
                    );

            if (userId.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "User ID required"
                        )
                );
            }

            // =========================
            // AI PROMPT
            // =========================

            String prompt = """
                    You are an advanced emotional AI engine
                    for a social media safety platform.

                    Analyze:
                    1. User comments
                    2. Scroll behavior
                    3. Watch time
                    4. Repeat views
                    5. Categories consumed

                    USER COMMENTS:
                    %s

                    SCROLLED CATEGORIES:
                    %s

                    WATCH TIME:
                    %d seconds

                    REPEAT VIEWS:
                    %d

                    Detect emotional state.

                    Return ONLY valid JSON:

                    {
                      "mood":"SAD",
                      "sadnessScore":80,
                      "angerScore":20,
                      "stressScore":70,
                      "happinessScore":10,
                      "confidenceScore":91,
                      "riskLevel":"HIGH",
                      "protectionMode":true,
                      "blockCategories":["violence","politics","depression"],
                      "boostCategories":["motivation","comedy","education"]
                    }
                    """.formatted(
                    recentComments,
                    scrolledCategories,
                    watchTime,
                    repeatViews
            );

            String aiResponse = callGroq(prompt, 300);

            JsonNode root = mapper.readTree(aiResponse);

            // =========================
            // PARSE AI RESPONSE
            // =========================

            String mood =
                    root.path("mood")
                            .asText("NORMAL")
                            .toUpperCase();

            Integer sadnessScore =
                    root.path("sadnessScore").asInt(0);

            Integer angerScore =
                    root.path("angerScore").asInt(0);

            Integer stressScore =
                    root.path("stressScore").asInt(0);

            Integer happinessScore =
                    root.path("happinessScore").asInt(0);

            Integer confidenceScore =
                    root.path("confidenceScore").asInt(50);

            String riskLevel =
                    root.path("riskLevel")
                            .asText("LOW");

            Boolean protectionMode =
                    root.path("protectionMode")
                            .asBoolean(false);

            List<String> blockCategories = new ArrayList<>();

            root.path("blockCategories")
                    .forEach(node ->
                            blockCategories.add(node.asText())
                    );

            List<String> boostCategories = new ArrayList<>();

            root.path("boostCategories")
                    .forEach(node ->
                            boostCategories.add(node.asText())
                    );

            // =========================
            // SAVE DATABASE
            // =========================

            UserMood userMood = new UserMood();

            userMood.setUserId(userId);
            userMood.setDetectedMood(mood);

            userMood.setSadnessScore(sadnessScore);
            userMood.setAngerScore(angerScore);
            userMood.setStressScore(stressScore);
            userMood.setHappinessScore(happinessScore);

            userMood.setConfidenceScore(confidenceScore);

            userMood.setRiskLevel(riskLevel);

            userMood.setProtectionMode(protectionMode);

            userMood.setBlockCategories(
                    String.join(",", blockCategories)
            );

            userMood.setBoostCategories(
                    String.join(",", boostCategories)
            );

            userMood.setLastUpdated(LocalDateTime.now());

            userMoodRepository.save(userMood);

            // =========================
            // RESPONSE
            // =========================

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "mood", mood,
                            "sadnessScore", sadnessScore,
                            "stressScore", stressScore,
                            "confidenceScore", confidenceScore,
                            "riskLevel", riskLevel,
                            "protectionMode", protectionMode,
                            "blockCategories", blockCategories,
                            "boostCategories", boostCategories
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        }
    }

    // =========================
    // GET USER MOOD STATUS
    // =========================
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getMoodStatus(
            @PathVariable String userId
    ) {

        Optional<UserMood> optionalMood =
                userMoodRepository.findById(userId);

        if (optionalMood.isEmpty()) {

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "mood", "NORMAL"
                    )
            );
        }

        UserMood mood = optionalMood.get();

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "mood", mood.getDetectedMood(),
                        "stressScore", mood.getStressScore(),
                        "sadnessScore", mood.getSadnessScore(),
                        "riskLevel", mood.getRiskLevel(),
                        "protectionMode", mood.getProtectionMode(),
                        "blockCategories",
                        Arrays.asList(
                                mood.getBlockCategories().split(",")
                        ),
                        "boostCategories",
                        Arrays.asList(
                                mood.getBoostCategories().split(",")
                        )
                )
        );
    }
}
