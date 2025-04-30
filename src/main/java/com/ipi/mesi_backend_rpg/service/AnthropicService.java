package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.configuration.AnthropicConfig;
import com.ipi.mesi_backend_rpg.dto.ai.AnthropicRequest;
import com.ipi.mesi_backend_rpg.dto.ai.AnthropicResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnthropicService {
    private final WebClient anthropicWebClient;
    private final AnthropicConfig anthropicConfig;

    public String generateContent(String systemPrompt, String userPrompt) {
        try {
            // Logging pour déboguer
            log.debug("Generating content with system: {} and user: {}", systemPrompt, userPrompt);

            AnthropicRequest request = AnthropicRequest.create(anthropicConfig.getDefaultModel(),
                    systemPrompt, userPrompt, 1000, 0.7);

            // Log de la requête curl pour déboguer si nécessaire
            logCurlCommand(anthropicConfig.getDefaultModel(), systemPrompt, userPrompt);

            AnthropicResponse response = anthropicWebClient.post()
                    .uri("/v1/messages")
                    .bodyValue(request)
                    .retrieve() // Commence la récupération de la réponse
                    .onStatus( // Gère les erreurs HTTP
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            log.error("API Error: {} - {}", clientResponse.statusCode(), errorBody);
                                            return Mono.error(new RuntimeException(
                                                    "API Error: " + clientResponse.statusCode() + " - " + errorBody));
                                        });
                            })
                    .bodyToMono(AnthropicResponse.class) // Convertit le corps en AnthropicResponse
                    .block(); // Bloque jusqu'à ce que la réponse soit disponible

            if (response != null) {
                return response.getTextContent();
            } else {
                return "Échec de génération: pas de réponse de l'API.";
            }
        } catch (Exception e) {
            log.error("Erreur lors de la génération de contenu avec Anthropic", e);
            return "Erreur technique lors de la génération: " + e.getMessage();
        }
    }

    private void logCurlCommand(String model, String systemPrompt, String userPrompt) {
        String json = String.format("""
                {
                  "model": "%s",
                  "system": "%s",
                  "messages": [
                    {"role": "user", "content": "%s"}
                  ],
                  "max_tokens": 1000,
                  "temperature": 0.7
                }
                """,
                model,
                systemPrompt.replace("\"", "\\\"").replace("\n", "\\n"),
                userPrompt.replace("\"", "\\\"").replace("\n", "\\n"));

        log.info("Commande curl pour test direct:\ncurl -X POST https://api.anthropic.com/v1/messages \\\n" +
                "-H \"x-api-key: VOTRE_CLÉ\" \\\n" +
                "-H \"anthropic-version: 2023-06-01\" \\\n" +
                "-H \"content-type: application/json\" \\\n" +
                "-d '{}'", json);
    }
}