package com.ipi.mesi_backend_rpg.service;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.ipi.mesi_backend_rpg.configuration.AnthropicConfig;
import com.ipi.mesi_backend_rpg.dto.ai.AnthropicRequest;
import com.ipi.mesi_backend_rpg.dto.ai.AnthropicResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnthropicService {
    private final WebClient anthropicWebClient;
    private final AnthropicConfig anthropicConfig;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int INITIAL_BACKOFF_SECONDS = 2;
    private static final int MAX_BACKOFF_SECONDS = 10;

    public String generateContent(String systemPrompt, String userPrompt) {
        try {
            log.debug("Generating content with system: {} and user: {}", systemPrompt, userPrompt);

            AnthropicRequest request = AnthropicRequest.create(anthropicConfig.getDefaultModel(),
                    systemPrompt, userPrompt, 1000, 0.7);

            logCurlCommand(anthropicConfig.getDefaultModel(), systemPrompt, userPrompt);

            // Utilisation de la stratégie de réessai
            AnthropicResponse response = anthropicWebClient.post()
                    .uri("/v1/messages")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            log.error("API Error: {} - {}", clientResponse.statusCode(), errorBody);

                                            // Si c'est une erreur de surcharge (529), on renvoie une exception
                                            // spécifique
                                            if (clientResponse.statusCode().value() == 529
                                                    && errorBody.contains("overloaded_error")) {
                                                return Mono.error(new AnthropicOverloadedException(
                                                        "Service Anthropic surchargé: " + errorBody));
                                            }

                                            return Mono.error(new RuntimeException(
                                                    "API Error: " + clientResponse.statusCode() + " - " + errorBody));
                                        });
                            })
                    .bodyToMono(AnthropicResponse.class)
                    // Configuration du retry - réessayer uniquement pour les erreurs de surcharge
                    .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(INITIAL_BACKOFF_SECONDS))
                            .maxBackoff(Duration.ofSeconds(MAX_BACKOFF_SECONDS))
                            .filter(throwable -> throwable instanceof AnthropicOverloadedException)
                            .doBeforeRetry(retrySignal -> {
                                log.info("Serveur Anthropic surchargé, nouvelle tentative ({}/{}), délai: {}s",
                                        retrySignal.totalRetries() + 1, MAX_RETRY_ATTEMPTS,
                                        INITIAL_BACKOFF_SECONDS * Math.pow(2, retrySignal.totalRetries()));
                            }))
                    .block();

            if (response != null) {
                return response.getTextContent();
            } else {
                return "Échec de génération: pas de réponse de l'API.";
            }
        } catch (Exception e) {
            log.error("Erreur lors de la génération de contenu avec Anthropic", e);

            // Message d'erreur convivial pour l'utilisateur
            if (e instanceof AnthropicOverloadedException
                    || (e.getCause() != null && e.getCause() instanceof AnthropicOverloadedException)) {
                return "Les serveurs d'IA sont actuellement très sollicités. Veuillez réessayer dans quelques instants.";
            }

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

        log.info("""
                Commande curl pour test direct:
                curl -X POST https://api.anthropic.com/v1/messages \\
                -H "x-api-key: VOTRE_CL\u00c9" \\
                -H "anthropic-version: 2023-06-01" \\
                -H "content-type: application/json" \\
                -d '{}'""", json);
    }

    private static class AnthropicOverloadedException extends RuntimeException {
        public AnthropicOverloadedException(String message) {
            super(message);
        }
    }
}