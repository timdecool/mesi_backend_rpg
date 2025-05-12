package com.ipi.mesi_backend_rpg.model;

public enum NotificationType {
    MODULE_SHARED,   // Quand un module est partagé
    MODULE_COMMENT,  // Quand un module reçoit un commentaire
    MODULE_LIKE,     // Quand un module est liké
    REVIEW_ADDED,    // Quand un avis est ajouté
    MENTION          // Quand un utilisateur est mentionné
}
