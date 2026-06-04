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
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/ai/mood")
@CrossOrigin("*")
public class AiMoodController {

    @Autowired
    private UserMoodRepository userMoodRepository;

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    // =========================================
    // RATE LIMITER (1 REQUEST / 1 MINUTE)
    // =========================================
    private static final Map<String, Long> userCooldownMap =
            new ConcurrentHashMap<String, Long>();
    // GROQ API CALL
    // =========================================
    private String callGroq(String prompt) throws Exception {

        String apiKey = System.getenv("GROQ_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("GROQ_API_KEY not configured");
        }

        ObjectNode requestJson = mapper.createObjectNode();

        // FAST + CHEAP MODEL
        requestJson.put("model", "llama-3.1-8b-instant");

        requestJson.put("max_tokens", 120);

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
                new HttpEntity<>(
                        mapper.writeValueAsString(requestJson),
                        headers
                );

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

        JsonNode root =
                mapper.readTree(response.getBody());

        return root.path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText()
                .trim();
    }

    // =========================================
    // ANALYZE MOOD
    // =========================================
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeMood(
            @RequestBody Map<String, Object> body
    ) {

        try {

            // =========================================
            // REQUEST DATA
            // =========================================

            String userId =
                    body.getOrDefault(
                            "userId",
                            ""
                    ).toString();

            String recentComments =
                    body.getOrDefault(
                            "recentComments",
                            ""
                    ).toString();

            String scrolledCategories =
                    body.getOrDefault(
                            "scrolledCategories",
                            ""
                    ).toString();

            int watchTime = 0;

            if (body.get("watchTime") != null) {

                watchTime =
                        Integer.parseInt(
                                body.get("watchTime")
                                        .toString()
                        );
            }

            int repeatViews = 0;

            if (body.get("repeatViews") != null) {

                repeatViews =
                        Integer.parseInt(
                                body.get("repeatViews")
                                        .toString()
                        );
            }

            if (userId.isBlank()) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "User ID required"
                        )
                );
            }

            // =========================================
            // RATE LIMIT CHECK
            // =========================================

            long now = System.currentTimeMillis();

            Long lastRequest =
                    userCooldownMap.get(userId);

            if (
                    lastRequest != null &&
                            (now - lastRequest) < 60000
            ) {

                return ResponseEntity.ok(
                        Map.of(
                                "success", true,
                                "cooldown", true,
                                "message",
                                "Mood already analyzed recently"
                        )
                );
            }

            userCooldownMap.put(userId, now);

            // =========================================
            // SIMPLE LOCAL MOOD DETECTION
            // =========================================

            String lowerComment =
                    recentComments.toLowerCase();

            String detectedMood = "NORMAL";

            int sadnessScore = 0;
            int stressScore = 0;
            int happinessScore = 50;

            if (
                    lowerComment.contains("sad") ||
                            lowerComment.contains("alone") ||
                            lowerComment.contains("depressed") ||
                            lowerComment.contains("cry")
            ) {

                detectedMood = "SAD";

                sadnessScore = 80;
                stressScore = 70;
                happinessScore = 20;
            }

            else if (
                    lowerComment.contains("angry") ||
                            lowerComment.contains("hate") ||
                            lowerComment.contains("annoyed")
            ) {

                detectedMood = "ANGRY";

                sadnessScore = 20;
                stressScore = 85;
                happinessScore = 10;
            }

            else if (
                    lowerComment.contains("happy") ||
                            lowerComment.contains("great") ||
                            lowerComment.contains("love")
            ) {

                detectedMood = "HAPPY";

                sadnessScore = 5;
                stressScore = 10;
                happinessScore = 90;
            }

            // =========================================
            // SHOULD USE AI?
            // =========================================

            boolean useAI =
                    watchTime >= 35 ||
                            repeatViews >= 2 ||
                            recentComments.length() > 20;

            List<String> blockCategories =
                    new ArrayList<>();

            List<String> boostCategories =
                    new ArrayList<>();

            String riskLevel = "LOW";

            boolean protectionMode = false;

            // =========================================
            // AI ANALYSIS
            // =========================================

            if (useAI) {

                String prompt = """
                        Analyze emotional state.

                        Comments: %s
                        Categories: %s
                        Watch Time: %d
                        Repeat Views: %d

                        Return ONLY valid JSON:
                        {
                          "mood":"SAD",
                          "sadnessScore":80,
                          "stressScore":70,
                          "happinessScore":20,
                          "riskLevel":"HIGH",
                          "protectionMode":true,
                          "blockCategories":["violence"],
                          "boostCategories":["motivation"]
                        }
                        """.formatted(
                        recentComments,
                        scrolledCategories,
                        watchTime,
                        repeatViews
                );

                String aiResponse =
                        callGroq(prompt);

                System.out.println(
                        "=========== AI RESPONSE ==========="
                );

                System.out.println(aiResponse);

                JsonNode root =
                        mapper.readTree(aiResponse);

                detectedMood =
                        root.path("mood")
                                .asText(detectedMood)
                                .toUpperCase();

                sadnessScore =
                        root.path("sadnessScore")
                                .asInt(sadnessScore);

                stressScore =
                        root.path("stressScore")
                                .asInt(stressScore);

                happinessScore =
                        root.path("happinessScore")
                                .asInt(happinessScore);

                riskLevel =
                        root.path("riskLevel")
                                .asText("LOW");

                protectionMode =
                        root.path("protectionMode")
                                .asBoolean(false);

                root.path("blockCategories")
                        .forEach(node ->
                                blockCategories.add(
                                        node.asText()
                                )
                        );

                root.path("boostCategories")
                        .forEach(node ->
                                boostCategories.add(
                                        node.asText()
                                )
                        );
            }

            // =========================================
            // FALLBACK CATEGORIES
            // =========================================

            if (boostCategories.isEmpty()) {

                boostCategories.add("motivation");
                boostCategories.add("education");
            }

            if (
                    detectedMood.equals("SAD") ||
                            detectedMood.equals("ANGRY")
            ) {

                protectionMode = true;

                blockCategories.add("violence");
                blockCategories.add("depression");
            }

            // =========================================
            // SAVE DATABASE
            // =========================================

            UserMood mood = new UserMood();

            mood.setUserId(userId);

            mood.setDetectedMood(detectedMood);

            mood.setSadnessScore(sadnessScore);

            mood.setStressScore(stressScore);

            mood.setHappinessScore(happinessScore);

            mood.setRiskLevel(riskLevel);

            mood.setProtectionMode(protectionMode);

            mood.setBlockCategories(
                    String.join(",", blockCategories)
            );

            mood.setBoostCategories(
                    String.join(",", boostCategories)
            );

            mood.setLastUpdated(LocalDateTime.now());

            userMoodRepository.save(mood);

            // =========================================
            // RESPONSE
            // =========================================

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "mood", detectedMood,
                            "sadnessScore", sadnessScore,
                            "stressScore", stressScore,
                            "happinessScore", happinessScore,
                            "riskLevel", riskLevel,
                            "protectionMode", protectionMode,
                            "blockCategories", blockCategories,
                            "boostCategories", boostCategories,
                            "aiUsed", useAI
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.ok(
                    Map.of(
                            "success", false,
                            "mood", "NORMAL",
                            "riskLevel", "LOW",
                            "protectionMode", false,
                            "message", e.getMessage()
                    )
            );
        }
    }

    // =========================================
    // GET USER MOOD STATUS
    // =========================================
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

        UserMood mood =
                optionalMood.get();

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "mood", mood.getDetectedMood(),
                        "sadnessScore", mood.getSadnessScore(),
                        "stressScore", mood.getStressScore(),
                        "happinessScore", mood.getHappinessScore(),
                        "riskLevel", mood.getRiskLevel(),
                        "protectionMode", mood.getProtectionMode(),
                        "blockCategories",
                        Arrays.asList(
                                mood.getBlockCategories().split(",")
                        ),
                        "boostCategories",
                        Arrays.asList(
                                mood.getBoostCategories().split(",")
                        ),
                        "lastUpdated",
                        mood.getLastUpdated()
                )
        );
    }
}