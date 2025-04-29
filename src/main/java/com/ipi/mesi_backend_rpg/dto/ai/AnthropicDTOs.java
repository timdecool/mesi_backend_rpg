package com.ipi.mesi_backend_rpg.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

public class AnthropicDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnthropicRequest {
        private String model;
        private String system;  // Ajout de ce champ system au premier niveau
        private List<Map<String, String>> messages;
        @JsonProperty("max_tokens")
        private int maxTokens;
        private double temperature;

        public static AnthropicRequest create(String model, String systemPrompt, String userPrompt, int maxTokens,
                double temperature) {
            return new AnthropicRequest(
                    model,
                    systemPrompt,  // Le prompt système est maintenant ici
                    List.of(Map.of("role", "user", "content", userPrompt)),  // Plus de message "system"
                    maxTokens,
                    temperature);
        }
    }

    @Data
    public static class AnthropicResponse {
        private String id;
        private String model;
        private List<Content> content;  // Changé pour une liste d'objets Content
        private String type;
        @JsonProperty("stop_reason")
        private String stopReason;
        private Usage usage;

        @Data
        public static class Content {
            private String type;
            private String text;
        }

        @Data
        public static class Usage {
            @JsonProperty("input_tokens")
            private int inputTokens;
            @JsonProperty("output_tokens")
            private int outputTokens;
        }

        public String getTextContent() {
            if (content != null && !content.isEmpty()) {
                return content.stream()
                        .filter(c -> "text".equals(c.getType()))
                        .map(Content::getText)
                        .findFirst()
                        .orElse("");
            }
            return "";
        }
    }
    
    // Ces classes sont inchangées
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIGenerationRequest {
        private String type; // paragraph, stat, music, etc.
        private Map<String, String> parameters;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIGenerationResponse {
        private String content;
        private String type;
    }
}