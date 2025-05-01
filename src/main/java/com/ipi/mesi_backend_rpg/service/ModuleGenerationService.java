package com.ipi.mesi_backend_rpg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModuleGenerationService {

    private final AnthropicService anthropicService;
    private final GameSystemRepository gameSystemRepository;
    private final AIBlockGenerationService aiBlockGenerationService;

    /**
     * Génère un module complet avec plusieurs types de blocs choisis par l'IA
     */
    public Map<String, Object> generateCompleteModule(Map<String, Object> map) {
        String theme = (String) map.getOrDefault("theme", "");
        String title = (String) map.getOrDefault("title", "");
        String description = (String) map.getOrDefault("description", "");
        Long gameSystemId = Long.valueOf(map.getOrDefault("gameSystemId", "1").toString());
        try {
            // Récupérer le système de jeu
            GameSystem gameSystem = gameSystemRepository.findById(gameSystemId)
                    .orElseThrow(() -> new RuntimeException("Système de jeu non trouvé avec l'ID: " + gameSystemId));

            // Générer une description ou utiliser celle fournie
            String moduleDescription = description;
            if (moduleDescription == null || moduleDescription.isEmpty()) {
                String descSystemPrompt = "Tu es un expert en création de modules de jeu de rôle.";
                String descUserPrompt = String.format(
                        "Écris une brève description (2-3 phrases) pour un module de jeu de rôle intitulé \"%s\" sur le thème \"%s\" pour le système %s.",
                        title, theme, gameSystem.getName());
                moduleDescription = anthropicService.generateContent(descSystemPrompt, descUserPrompt);
            }

            // Demander à l'IA de générer la structure globale du module
            String structureSystemPrompt = """
                    Tu es un expert en conception de modules de jeu de rôle.
                    Ta tâche est de concevoir la structure d'un module de jeu de rôle complet.
                    Réponds uniquement au format JSON, avec la structure précise indiquée dans l'instruction.
                    """;

            String structureUserPrompt = String.format(
                    """
                            Crée la structure d'un module de jeu de rôle intitulé "%s" sur le thème "%s" pour le système %s.

                            Définis entre 5 et 8 blocs de contenu pour ce module, en choisissant parmi ces types:
                            - paragraph: pour du texte narratif ou descriptif
                            - stat: pour des statistiques de personnages, monstres ou objets
                            - music: pour suggérer des ambiances musicales pour certaines scènes

                            Pour chaque bloc, donne un titre pertinent.

                            Le format JSON attendu est:
                            {
                              "title": "Titre du module",
                              "description": "Description du module",
                              "blocks": [
                                {
                                  "type": "paragraph|stat|music",
                                  "title": "Titre du bloc",
                                  "focus": "Description spécifique du contenu à générer pour ce bloc"
                                }
                              ]
                            }

                            Assure-toi que le JSON généré est complet et valide. Inclus une variété de types de blocs pour créer un module équilibré.
                            """,
                    title, theme, gameSystem.getName());

            String structureJson = anthropicService.generateContent(structureSystemPrompt, structureUserPrompt);

            // Parser la structure JSON
            // Note: Dans une implémentation réelle, utilisez Jackson ObjectMapper
            // Ici simulé avec un exemple simple pour illustrer
            Map<String, Object> parsedStructure = parseJson(structureJson);
            List<Map<String, Object>> blockStructures = getBlocksFromParsedStructure(parsedStructure);

            // Générer chaque bloc de contenu en parallèle
            List<CompletableFuture<Map<String, Object>>> blockFutures = new ArrayList<>();

            for (Map<String, Object> blockStructure : blockStructures) {
                String blockType = (String) blockStructure.get("type");
                String blockTitle = (String) blockStructure.get("title");
                String blockFocus = (String) blockStructure.get("focus");

                blockFutures.add(CompletableFuture
                        .supplyAsync(() -> generateBlockContent(blockType, blockTitle, blockFocus, gameSystem)));
            }

            // Attendre que tous les blocs soient générés
            CompletableFuture.allOf(blockFutures.toArray(CompletableFuture[]::new)).join();

            // Collecter les résultats
            List<Map<String, Object>> generatedBlocks = new ArrayList<>();
            for (CompletableFuture<Map<String, Object>> future : blockFutures) {
                generatedBlocks.add(future.get());
            }

            // Assembler la réponse finale
            Map<String, Object> response = new HashMap<>();
            response.put("title", title);
            response.put("description", moduleDescription);
            response.put("gameSystemId", gameSystemId);
            response.put("blocks", generatedBlocks);

            return response;

        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors de la génération du module complet", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur technique: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Génère le contenu d'un bloc spécifique
     */
    private Map<String, Object> generateBlockContent(String blockType, String blockTitle, String blockFocus,
            GameSystem gameSystem) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", blockType);
        result.put("title", blockTitle);

        try {
            switch (blockType.toLowerCase()) {
                case "paragraph" -> {
                    Map<String, String> paragraphParams = new HashMap<>();
                    paragraphParams.put("context", blockFocus);
                    paragraphParams.put("tone", "narratif");
                    paragraphParams.put("gameSystem", gameSystem.getName());
                    paragraphParams.put("gameSystemId", gameSystem.getId().toString());

                    String paragraphContent = aiBlockGenerationService.generateParagraphBlock(paragraphParams);
                    result.put("content", paragraphContent);
                    result.put("style", "narrative");
                }

                case "stat" -> {
                    Map<String, String> statParams = new HashMap<>();
                    statParams.put("entityType", blockFocus);
                    statParams.put("entityName", "");
                    statParams.put("powerLevel", "moyen");
                    statParams.put("gameSystem", gameSystem.getName());
                    statParams.put("gameSystemId", gameSystem.getId().toString());

                    String statContent = aiBlockGenerationService.generateStatBlock(statParams);
                    result.put("statValues", statContent);
                    result.put("statRules", "");
                }

                case "music" -> {
                    Map<String, String> musicParams = new HashMap<>();
                    musicParams.put("scene", blockFocus);
                    musicParams.put("atmosphere", "immersive");

                    String musicContent = aiBlockGenerationService.generateMusicDescription(musicParams);
                    result.put("label", "Ambiance pour " + blockTitle);
                    result.put("description", musicContent);
                    result.put("src", "");
                }

                default -> result.put("content", "Type de bloc non supporté: " + blockType);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la génération du bloc de type {}: {}", blockType, e.getMessage());
            result.put("error", "Erreur de génération: " + e.getMessage());
        }

        return result;
    }

    // Méthodes utilitaires pour parser le JSON
    // Note: Dans une implémentation réelle, utilisez Jackson ObjectMapper
    private Map<String, Object> parseJson(String json) {
        // Simulation simple du parsing JSON
        Map<String, Object> result = new HashMap<>();
        result.put("title", "Titre extrait du JSON");
        result.put("description", "Description extraite du JSON");

        List<Map<String, Object>> blocks = new ArrayList<>();
        // Simuler quelques blocs extraits du JSON
        blocks.add(createDummyBlock("paragraph", "Introduction", "Introduction au module"));
        blocks.add(createDummyBlock("paragraph", "Lieu principal", "Description du lieu principal"));
        blocks.add(createDummyBlock("stat", "Antagoniste", "Statistiques de l'antagoniste principal"));
        blocks.add(createDummyBlock("music", "Ambiance de combat", "Suggestion musicale pour les combats"));
        blocks.add(createDummyBlock("paragraph", "Conclusion", "Fin de l'aventure"));

        result.put("blocks", blocks);
        return result;
    }

    private List<Map<String, Object>> getBlocksFromParsedStructure(Map<String, Object> structure) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> blocks = (List<Map<String, Object>>) structure.get("blocks");
        return blocks != null ? blocks : new ArrayList<>();
    }

    private Map<String, Object> createDummyBlock(String type, String title, String focus) {
        Map<String, Object> block = new HashMap<>();
        block.put("type", type);
        block.put("title", title);
        block.put("focus", focus);
        return block;
    }
}