package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIBlockGenerationService {

    private final AnthropicService anthropicService;
    private final GameSystemRepository gameSystemRepository;

    public String generateParagraphBlock(Map<String, String> parameters) {
        String gameSystem = parameters.getOrDefault("gameSystem", "fantasy");
        String tone = parameters.getOrDefault("tone", "descriptif");
        String context = parameters.getOrDefault("context", "");
        String characters = parameters.getOrDefault("characters", "");
        
        String systemPrompt = """
            Tu es un expert en création de scénarios pour jeux de rôle. 
            Tu dois générer un paragraphe narratif immersif qui pourra être utilisé dans un scénario.
            Utilise un style d'écriture évocateur qui capte l'imagination du joueur.
            Ne mentionne pas que c'est généré par IA.
            """;
            
        String userPrompt = String.format("""
            Écris un paragraphe narratif pour un univers de %s.
            Ton: %s
            Contexte: %s
            Personnages impliqués: %s
            
            Le paragraphe doit faire entre 150 et 300 mots, être immersif et donner envie au maître du jeu de développer cette scène.
            """, gameSystem, tone, context, characters);
            
        return anthropicService.generateContent(systemPrompt, userPrompt);
    }
    
    public String generateStatBlock(Map<String, String> parameters) {
        String gameSystem = parameters.getOrDefault("gameSystem", "Donjon et Dragon");
        Long gameSystemId = Long.parseLong(parameters.getOrDefault("gameSystemId", "1"));
        String entityType = parameters.getOrDefault("entityType", "monster");
        String entityName = parameters.getOrDefault("entityName", "");
        String powerLevel = parameters.getOrDefault("powerLevel", "moyen");
        
        GameSystem system = gameSystemRepository.findById(gameSystemId)
                .orElse(null);
        
        String systemName = system != null ? system.getName() : gameSystem;
        
        String systemPrompt = """
            Tu es un expert en création de fiches de statistiques pour jeux de rôle.
            Génère une fiche de statistiques au format JSON pour le système indiqué.
            Inclus uniquement les attributs pertinents pour ce système de jeu.
            Les statistiques doivent être équilibrées et réalistes pour le niveau de puissance demandé.
            """;
            
        String userPrompt = String.format("""
            Crée une fiche de statistiques au format JSON pour un %s nommé "%s" dans le système de jeu %s.
            Niveau de puissance: %s
            
            Inclus tous les attributs pertinents comme les points de vie, attaques, défense, etc.
            Le format JSON doit être simple et facile à interpréter par un humain.
            """, entityType, entityName, systemName, powerLevel);
            
        return anthropicService.generateContent(systemPrompt, userPrompt);
    }

    public String generateMusicDescription(Map<String, String> parameters) {
        String atmosphere = parameters.getOrDefault("atmosphere", "mystérieuse");
        String scene = parameters.getOrDefault("scene", "exploration");
        
        String systemPrompt = """
            Tu es un expert en ambiance sonore pour jeux de rôle.
            Ton rôle est de décrire précisément une piste musicale qui correspondrait parfaitement à une scène donnée.
            """;
            
        String userPrompt = String.format("""
            Décris en 2-3 phrases une piste musicale idéale pour une scène: %s
            avec une atmosphère: %s
            
            Précise le style musical, les instruments dominants, le tempo, et l'émotion générale que cette musique devrait susciter.
            """, scene, atmosphere);
            
        return anthropicService.generateContent(systemPrompt, userPrompt);
    }
}