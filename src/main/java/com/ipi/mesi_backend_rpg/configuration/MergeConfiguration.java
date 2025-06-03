package com.ipi.mesi_backend_rpg.configuration;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.merge")
@Getter
@Setter
public class MergeConfiguration {

    /**
     * Durée de vie des conflits en cache (en minutes)
     */
    private int conflictCacheTimeoutMinutes = 30;

    /**
     * Nombre maximum de conflits actifs par utilisateur
     */
    private int maxActiveConflictsPerUser = 10;

    /**
     * Activer/désactiver la résolution automatique
     */
    private boolean autoResolutionEnabled = true;

    /**
     * Stratégie par défaut pour la résolution automatique
     */
    private String defaultAutoResolutionStrategy = "auto_merge";

    /**
     * Champs qui peuvent être fusionnés automatiquement
     */
    private Set<String> autoMergeableFields = Set.of(
            "description",
            "title",
            "published");

    /**
     * Champs qui ne doivent jamais être fusionnés automatiquement
     */
    private Set<String> nonAutoMergeableFields = Set.of(
            "id",
            "createdAt",
            "updatedAt",
            "creator");

    /**
     * Configuration des stratégies de résolution par type de champ
     */
    private Map<String, FieldMergeStrategy> fieldStrategies = Map.of(
            "string", new FieldMergeStrategy("user", true, "Prendre la valeur de l'utilisateur pour les textes"),
            "number", new FieldMergeStrategy("current", true, "Garder la valeur actuelle pour les nombres"),
            "boolean", new FieldMergeStrategy("user", true, "Prendre la valeur de l'utilisateur pour les booléens"),
            "date",
            new FieldMergeStrategy("current", false, "Les dates ne peuvent pas être fusionnées automatiquement"));

    /**
     * Activer les notifications de conflit via WebSocket
     */
    private boolean conflictNotificationsEnabled = true;

    /**
     * Activer la création de sauvegardes avant fusion
     */
    private boolean backupBeforeMergeEnabled = true;

    /**
     * Délai avant nettoyage automatique des conflits résolus (en heures)
     */
    private int resolvedConflictCleanupHours = 24;

    /**
     * Nombre maximum de tentatives de résolution automatique
     */
    private int maxAutoResolutionAttempts = 3;

    /**
     * Configuration pour les conflits de blocs
     */
    private BlockConflictConfig blockConflictConfig = new BlockConflictConfig();

    /**
     * Configuration spécifique aux conflits de blocs
     */
    @Getter
    @Setter
    public static class BlockConflictConfig {
        private boolean enableBlockOrderConflictDetection = true;
        private boolean autoResolveBlockOrderConflicts = false;
        private String blockOrderResolutionStrategy = "user"; // "user", "current", "merge"
        private boolean enableBlockContentComparison = true;
        private double blockSimilarityThreshold = 0.8; // Seuil de similarité pour considérer deux blocs comme
                                                       // similaires
    }

    /**
     * Stratégie de fusion pour un type de champ
     */
    @Getter
    @Setter
    public static class FieldMergeStrategy {
        private String defaultResolution; // "user", "current", "merge", "manual"
        private boolean canAutoResolve;
        private String description;

        public FieldMergeStrategy() {
        }

        public FieldMergeStrategy(String defaultResolution, boolean canAutoResolve, String description) {
            this.defaultResolution = defaultResolution;
            this.canAutoResolve = canAutoResolve;
            this.description = description;
        }
    }

    /**
     * Obtient la durée de timeout sous forme de Duration
     */
    public Duration getConflictCacheTimeout() {
        return Duration.ofMinutes(conflictCacheTimeoutMinutes);
    }

    /**
     * Obtient la durée de nettoyage sous forme de Duration
     */
    public Duration getResolvedConflictCleanupDuration() {
        return Duration.ofHours(resolvedConflictCleanupHours);
    }

    /**
     * Détermine si un champ peut être fusionné automatiquement
     */
    public boolean canFieldAutoMerge(String fieldName, String fieldType) {
        if (nonAutoMergeableFields.contains(fieldName)) {
            return false;
        }

        if (autoMergeableFields.contains(fieldName)) {
            return true;
        }

        FieldMergeStrategy strategy = fieldStrategies.get(fieldType.toLowerCase());
        return strategy != null && strategy.canAutoResolve;
    }

    /**
     * Obtient la stratégie de résolution par défaut pour un type de champ
     */
    public String getFieldDefaultResolution(String fieldType) {
        FieldMergeStrategy strategy = fieldStrategies.get(fieldType.toLowerCase());
        return strategy != null ? strategy.defaultResolution : defaultAutoResolutionStrategy;
    }

    /**
     * Valide la configuration
     */
    public void validateConfiguration() {
        if (conflictCacheTimeoutMinutes <= 0) {
            throw new IllegalArgumentException("Le timeout des conflits doit être positif");
        }

        if (maxActiveConflictsPerUser <= 0) {
            throw new IllegalArgumentException("Le nombre maximum de conflits par utilisateur doit être positif");
        }

        if (!Set.of("auto_merge", "take_user", "take_current", "manual").contains(defaultAutoResolutionStrategy)) {
            throw new IllegalArgumentException(
                    "Stratégie de résolution automatique invalide: " + defaultAutoResolutionStrategy);
        }

        if (blockConflictConfig.blockSimilarityThreshold < 0.0 || blockConflictConfig.blockSimilarityThreshold > 1.0) {
            throw new IllegalArgumentException("Le seuil de similarité des blocs doit être entre 0.0 et 1.0");
        }
    }
}   