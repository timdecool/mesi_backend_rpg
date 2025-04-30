package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModuleGenerationService {

    private final AnthropicService anthropicService;
    private final GameSystemRepository gameSystemRepository;
    private final AIBlockGenerationService aiBlockGenerationService;

    /**
     * Génère un module complet avec plusieurs types de blocs
     */
    public Map<String, Object> generateCompleteModule(String theme, String title, String description, Long gameSystemId, Long userId) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> blocks = new ArrayList<>();
        
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
            
            // Générer plusieurs blocs en parallèle pour de meilleures performances
            CompletableFuture<String> introFuture = CompletableFuture.supplyAsync(() -> {
                Map<String, String> params = new HashMap<>();
                params.put("context", "Introduction pour un module intitulé \"" + title + "\" sur le thème \"" + theme + "\"");
                params.put("tone", "narratif");
                params.put("gameSystem", gameSystem.getName());
                params.put("gameSystemId", gameSystemId.toString());
                return aiBlockGenerationService.generateBlock("paragraph", params);
            });
            
            CompletableFuture<String> locationFuture = CompletableFuture.supplyAsync(() -> {
                Map<String, String> params = new HashMap<>();
                params.put("context", "Description du lieu principal pour un module sur le thème \"" + theme + "\"");
                params.put("tone", "descriptif");
                params.put("gameSystem", gameSystem.getName());
                params.put("gameSystemId", gameSystemId.toString());
                return aiBlockGenerationService.generateBlock("paragraph", params);
            });
            
            CompletableFuture<String> antagonistFuture = CompletableFuture.supplyAsync(() -> {
                Map<String, String> params = new HashMap<>();
                params.put("entityType", "antagoniste principal");
                params.put("entityName", "Antagoniste de " + title);
                params.put("powerLevel", "élevé");
                params.put("gameSystem", gameSystem.getName());
                params.put("gameSystemId", gameSystemId.toString());
                return aiBlockGenerationService.generateBlock("stat", params);
            });
            
            CompletableFuture<String> musicFuture = CompletableFuture.supplyAsync(() -> {
                Map<String, String> params = new HashMap<>();
                params.put("scene", "Scène principale du module \"" + title + "\"");
                params.put("atmosphere", "dramatique");
                return aiBlockGenerationService.generateBlock("music", params);
            });
            
            // Attendre que toutes les générations soient terminées
            CompletableFuture.allOf(introFuture, locationFuture, antagonistFuture, musicFuture).join();
            
            // Créer les blocs avec les résultats
            Map<String, Object> introBlock = new HashMap<>();
            introBlock.put("type", "paragraph");
            introBlock.put("title", "Introduction");
            introBlock.put("content", introFuture.get());
            introBlock.put("style", "narrative");
            blocks.add(introBlock);
            
            Map<String, Object> locationBlock = new HashMap<>();
            locationBlock.put("type", "paragraph");
            locationBlock.put("title", "Lieu principal");
            locationBlock.put("content", locationFuture.get());
            locationBlock.put("style", "descriptive");
            blocks.add(locationBlock);
            
            Map<String, Object> antagonistBlock = new HashMap<>();
            antagonistBlock.put("type", "stat");
            antagonistBlock.put("title", "Antagoniste principal");
            antagonistBlock.put("statValues", antagonistFuture.get());
            antagonistBlock.put("statRules", "");
            blocks.add(antagonistBlock);
            
            Map<String, Object> musicBlock = new HashMap<>();
            musicBlock.put("type", "music");
            musicBlock.put("title", "Ambiance musicale");
            musicBlock.put("label", "Thème principal");
            musicBlock.put("description", musicFuture.get());
            musicBlock.put("src", "");
            blocks.add(musicBlock);
            
            // Assembler la réponse
            response.put("title", title);
            response.put("description", moduleDescription);
            response.put("gameSystemId", gameSystemId);
            response.put("blocks", blocks);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Erreur lors de la génération du module complet", e);
            response.put("error", "Erreur technique: " + e.getMessage());
        }
        
        return response;
    }
}