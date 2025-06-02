package com.ipi.mesi_backend_rpg.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldConflict {
    private String fieldName;
    private Object originalValue; // Valeur dans la version de base
    private Object currentValue; // Valeur actuelle en DB
    private Object userValue; // Valeur que l'utilisateur veut appliquer
    private ConflictType type;

    // Métadonnées supplémentaires
    private Long lastModifiedBy; // ID de l'utilisateur qui a modifié en dernier
    private java.time.LocalDateTime lastModifiedAt;
    private String description; // Description humaine du conflit
}