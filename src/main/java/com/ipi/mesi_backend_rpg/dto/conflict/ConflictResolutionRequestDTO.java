package com.ipi.mesi_backend_rpg.dto.conflict;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConflictResolutionRequestDTO {
    private String conflictId;
    private String resolutionStrategy; // "auto", "manual", "cancel"

    // Résolutions par champ
    private List<FieldResolutionDTO> fieldResolutions;

    // Options globales
    private boolean forceOverwrite; // Forcer l'écrasement même en cas de nouveaux conflits
    private boolean createBackup; // Créer une sauvegarde avant application
    private String comment; // Commentaire de résolution

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldResolutionDTO {
        private String fieldName;
        private String resolution; // "current", "user", "custom"
        private Object customValue; // Valeur personnalisée si resolution = "custom"
        private Map<String, Object> options; // Options spécifiques au champ
    }
}
