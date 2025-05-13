package com.ipi.mesi_backend_rpg.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Récupère l'utilisateur actuellement authentifié
     * @return L'utilisateur actuel ou null si non authentifié
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        // L'identifiant de l'utilisateur (UID Firebase ou identifiant interne)
        // est stocké comme le nom du principal
        String userId = authentication.getName();
        
        // Vérifiez si c'est un UID Firebase ou un ID numérique interne
        if (userId.matches("\\d+")) {
            // C'est un ID numérique interne, on le convertit en Long
            return userRepository.findById(Long.parseLong(userId)).orElse(null);
        } else {
            // C'est probablement un UID Firebase, cherchons par email
            // Puisque dans votre filtre Firebase, vous avez déjà fait la correspondance
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                org.springframework.security.core.userdetails.User userDetails = 
                    (org.springframework.security.core.userdetails.User) principal;
                return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            }
        }
        
        return null;
    }
}