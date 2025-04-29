package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.configuration.AnthropicConfig;
import com.ipi.mesi_backend_rpg.dto.ai.AnthropicDTOs.AnthropicRequest;
import com.ipi.mesi_backend_rpg.dto.ai.AnthropicDTOs.AnthropicResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

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
            
            // Important: Créer manuellement l'objet au lieu d'utiliser create()
            AnthropicRequest request = new AnthropicRequest();
            request.setModel(anthropicConfig.getDefaultModel());
            request.setSystem(systemPrompt); // Système comme champ de premier niveau
            request.setMessages(List.of(Map.of("role", "user", "content", userPrompt)));
            request.setMaxTokens(1000);
            request.setTemperature(0.7);
            
            // Log de la requête curl pour déboguer si nécessaire
            logCurlCommand(anthropicConfig.getDefaultModel(), systemPrompt, userPrompt);
    
            AnthropicResponse response = anthropicWebClient.post()
                    .uri("/v1/messages")
                    .bodyValue(request)
                    .retrieve()
                    // Correction pour la vérification d'erreur - utilisez la syntaxe adaptée à votre version de Spring
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("API Error: {} - {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("API Error: " + clientResponse.statusCode() + " - " + errorBody));
                                });
                    })
                    .bodyToMono(AnthropicResponse.class)
                    .block();
    
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
            userPrompt.replace("\"", "\\\"").replace("\n", "\\n")
        );
        
        log.info("Commande curl pour test direct:\ncurl -X POST https://api.anthropic.com/v1/messages \\\n" +
                 "-H \"x-api-key: VOTRE_CLÉ\" \\\n" +
                 "-H \"anthropic-version: 2023-06-01\" \\\n" +
                 "-H \"content-type: application/json\" \\\n" +
                 "-d '{}'", json);
    }
}