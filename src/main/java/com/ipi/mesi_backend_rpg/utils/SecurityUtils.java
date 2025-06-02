package com.ipi.mesi_backend_rpg.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtils {
    
    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    
    /**
     * Récupère l'ID de l'utilisateur connecté depuis le contexte Spring Security
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                
                if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    // Dans votre FirebaseAuthenticationFilter, vous stockez l'UID dans username
                    return Long.valueOf(userDetails.getUsername());
                }
            }
            
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'ID utilisateur", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Erreur d'authentification");
        }
    }
    
    /**
     * Récupère l'ID utilisateur depuis une requête HTTP (avec token Firebase)
     */
    public Long getCurrentUserIdFromRequest(HttpServletRequest request) {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token d'authentification manquant");
            }
            
            String token = authorizationHeader.substring(7);
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
            String email = decodedToken.getEmail();
            
            // Trouver l'utilisateur dans votre base de données par email
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isPresent()) {
                return userOpt.get().getId();
            } else {
                // Utilisateur Firebase mais pas dans votre DB - créer automatiquement ?
                log.warn("Utilisateur Firebase trouvé mais absent de la DB: {}", email);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé en base de données");
            }
            
        } catch (FirebaseAuthException e) {
            log.error("Erreur de validation du token Firebase", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Firebase invalide");
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur d'authentification");
        }
    }
    
    /**
     * Récupère l'ID utilisateur depuis les headers WebSocket
     */
    public Long getCurrentUserIdFromWebSocket(StompHeaderAccessor headerAccessor) {
        try {
            // Dans votre WebSocketSecurityConfig, vous stockez l'ID utilisateur dans le Principal
            if (headerAccessor.getUser() != null) {
                String userId = headerAccessor.getUser().getName();
                return Long.valueOf(userId);
            }
            
            // Fallback : essayer de récupérer depuis les headers natifs
            String authToken = headerAccessor.getFirstNativeHeader("Authorization");
            if (authToken != null && authToken.startsWith("Bearer ")) {
                String token = authToken.substring(7);
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
                String email = decodedToken.getEmail();
                
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    return userOpt.get().getId();
                }
            }
            
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié via WebSocket");
            
        } catch (FirebaseAuthException e) {
            log.error("Erreur de validation du token Firebase via WebSocket", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Firebase invalide");
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification WebSocket", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Erreur d'authentification WebSocket");
        }
    }
    
    /**
     * Récupère l'entité User complète de l'utilisateur connecté
     */
    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
    }
    
    /**
     * Récupère l'entité User depuis une requête HTTP
     */
    public User getCurrentUserFromRequest(HttpServletRequest request) {
        Long userId = getCurrentUserIdFromRequest(request);
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
    }
    
    /**
     * Vérifie si l'utilisateur connecté a un rôle spécifique
     */
    public boolean hasRole(String role) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && 
                   authentication.getAuthorities().stream()
                       .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Vérifie si l'utilisateur connecté est propriétaire d'une ressource
     */
    public boolean isResourceOwner(Long resourceOwnerId) {
        try {
            Long currentUserId = getCurrentUserId();
            return currentUserId.equals(resourceOwnerId);
        } catch (Exception e) {
            return false;
        }
    }
}