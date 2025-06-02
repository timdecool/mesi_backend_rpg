package com.ipi.mesi_backend_rpg.model;

public enum ConflictType {
    FIELD_CONFLICT, // Conflit sur un champ spécifique
    BLOCK_ORDER_CONFLICT, // Conflit sur l'ordre des blocs
    BLOCK_DELETED_MODIFIED, // Un bloc supprimé d'un côté, modifié de l'autre
    BLOCK_ADDED_CONFLICT, // Conflit lors d'ajout de blocs au même endroit
    ACCESS_RIGHTS_CONFLICT, // Conflit sur les droits d'accès
    VERSION_CONFLICT // Conflit de version générale
}
