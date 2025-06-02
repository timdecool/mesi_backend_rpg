package com.ipi.mesi_backend_rpg.dto.conflict;

import java.time.LocalDateTime;

import com.ipi.mesi_backend_rpg.model.ConflictType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldConflictDTO {
    private String fieldName;
    private String fieldDisplayName; // Nom d'affichage pour l'UI
    private Object originalValue;
    private Object currentValue;
    private Object userValue;
    private ConflictType type;

    // Métadonnées pour l'affichage
    private String fieldType; // "text", "number", "boolean", "object", etc.
    private boolean canAutoResolve; // Si le conflit peut être résolu automatiquement
    private String autoResolveStrategy; // "keep_current", "take_user", "merge", etc.

    // Informations contextuelles
    private Long lastModifiedBy;
    private String lastModifiedByUsername;
    private LocalDateTime lastModifiedAt;
    private String description;

    // Résolution suggérée
    private String suggestedResolution; // "current", "user", "manual"
    private String resolutionReason; // Explication de la suggestion
}
