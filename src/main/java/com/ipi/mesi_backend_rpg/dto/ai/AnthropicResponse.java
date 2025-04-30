package com.ipi.mesi_backend_rpg.dto.ai;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ipi.mesi_backend_rpg.enums.EBlockType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnthropicResponse {
    private String id;
        private String model;
        private List<Content> content;  // Changé pour une liste d'objets Content
        private EBlockType type;
        @JsonProperty("stop_reason")
        private String stopReason;
        private Usage usage;

        @Getter
        @Setter
        public static class Content {
            private String type;
            private String text;
        }

        @Getter
        @Setter
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
