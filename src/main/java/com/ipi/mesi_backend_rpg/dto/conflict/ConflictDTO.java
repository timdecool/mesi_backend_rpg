package com.ipi.mesi_backend_rpg.dto.conflict;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.ipi.mesi_backend_rpg.model.ConflictType;
import com.ipi.mesi_backend_rpg.model.ResourceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConflictDTO {
    private String conflictId;
    private ResourceType resourceType;
    private Long resourceId;
    private ConflictType type;
    private String description;

    // Informations sur les versions en conflit
    private Long originalVersion;
    private Long currentVersion;
    private Long userVersion;

    // Informations sur les utilisateurs
    private Long currentUserId;
    private String currentUsername;
    private Long conflictingUserId;
    private String conflictingUsername;
    private LocalDateTime conflictCreatedAt;

    // Détails des conflits par champ
    private List<FieldConflictDTO> fieldConflicts;

    // Métadonnées supplémentaires (pour les conflits complexes comme les blocs)
    private Map<String, Object> metadata;

    // Statut du conflit
    private ConflictStatus status;

    public enum ConflictStatus {
        DETECTED, // Conflit détecté
        REVIEWING, // En cours de révision par l'utilisateur
        RESOLVED, // Résolu
        CANCELLED // Annulé
    }
}
