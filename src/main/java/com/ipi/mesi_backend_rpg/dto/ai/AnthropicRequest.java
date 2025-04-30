package com.ipi.mesi_backend_rpg.dto.ai;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnthropicRequest {
    private String model;
    private String system;
    private List<Map<String, String>> messages;
    @JsonProperty("max_tokens")
    private int maxTokens;
    private double temperature;

    public static AnthropicRequest create(String model, String systemPrompt, String userPrompt, int maxTokens,
            double temperature) {
        return new AnthropicRequest(
                model,
                systemPrompt, // Le prompt système est maintenant ici
                List.of(Map.of("role", "user", "content", userPrompt)), // Plus de message "system"
                maxTokens,
                temperature);
    }
}
