package com.social.Config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    // ✅ Main ChatClient Bean (BEST PRACTICE)
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    // ✅ Optional: If you want direct Google model access
    @Bean
    public GoogleGenAiChatModel googleGenAiChatModel(GoogleGenAiChatModel model) {
        return model;
    }
}