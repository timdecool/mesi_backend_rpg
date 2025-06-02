package com.ipi.mesi_backend_rpg.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.model.EditLock;
import com.ipi.mesi_backend_rpg.model.ResourceType;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.EditLockRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EditLockService {
    
    private final EditLockRepository editLockRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Configuration
    private static final int DEFAULT_LOCK_DURATION_MINUTES = 10;
    private static final int MAX_LOCKS_PER_USER = 10;
    private static final int LOCK_EXTENSION_MINUTES = 5;
    
    // Classes de résultat
    @Getter
    @AllArgsConstructor
    public static class LockResult {
        private final boolean acquired;
        private final String lockToken;
        private final EditLock conflictingLock;
        private final String message;
        private final LocalDateTime expiresAt;
        
        public static LockResult success(String lockToken, LocalDateTime expiresAt) {
            return new LockResult(true, lockToken, null, "Lock acquis avec succès", expiresAt);
        }
        
        public static LockResult conflict(EditLock conflictingLock, String message) {
            return new LockResult(false, null, conflictingLock, message, null);
        }
        
        public static LockResult error(String message) {
            return new LockResult(false, null, null, message, null);
        }
    }
    
    @Getter
    @AllArgsConstructor
    public static class LockInfo {
        private final String lockToken;
        private final ResourceType resourceType;
        private final Long resourceId;
        private final String lockScope;
        private final String lockedByUsername;
        private final LocalDateTime lockedAt;
        private final LocalDateTime expiresAt;
        private final boolean isOwnLock;
    }
    
    // Nettoyage automatique des locks expirés
    @Scheduled(fixedRate = 60000) // Chaque minute
    public void cleanupExpiredLocks() {
        try {
            editLockRepository.deleteExpiredLocks(LocalDateTime.now());
            log.debug("Nettoyage des locks expirés effectué");
        } catch (Exception e) {
            log.error("Erreur lors du nettoyage des locks expirés", e);
        }
    }
    
    // Acquérir un lock
    public LockResult acquireLock(ResourceType resourceType, Long resourceId, 
                                 Long userId, String lockScope) {
        return acquireLock(resourceType, resourceId, userId, lockScope, DEFAULT_LOCK_DURATION_MINUTES);
    }
    
    public LockResult acquireLock(ResourceType resourceType, Long resourceId, 
                                 Long userId, String lockScope, int durationMinutes) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + userId));
        
        // Nettoyer les locks expirés
        editLockRepository.deleteExpiredLocks(LocalDateTime.now());
        
        // Vérifier le nombre de locks de l'utilisateur
        long userActiveLocks = editLockRepository.findActiveLocksForUser(user, LocalDateTime.now()).size();
        if (userActiveLocks >= MAX_LOCKS_PER_USER) {
            return LockResult.error("Trop de locks actifs. Libérez d'abord d'autres ressources.");
        }
        
        // Vérifier s'il y a déjà un lock pour cette ressource
        List<EditLock> existingLocks = editLockRepository.findNonExpiredLocksForResource(
            resourceType, resourceId, LocalDateTime.now());
        
        for (EditLock existingLock : existingLocks) {
            // Si c'est le même utilisateur, renouveler ou créer un nouveau selon le scope
            if (existingLock.getLockedBy().getId().equals(userId)) {
                if (canCoexist(existingLock.getLockScope(), lockScope)) {
                    // Les scopes peuvent coexister, créer un nouveau lock
                    break;
                } else {
                    // Renouveler le lock existant
                    existingLock.extend(durationMinutes);
                    editLockRepository.save(existingLock);
                    
                    notifyLockRenewed(existingLock);
                    return LockResult.success(existingLock.getLockToken(), existingLock.getExpiresAt());
                }
            } else {
                // Lock détenu par quelqu'un d'autre
                if (!canCoexist(existingLock.getLockScope(), lockScope)) {
                    String message = String.format("Ressource verrouillée par %s jusqu'à %s", 
                        existingLock.getLockedBy().getUsername(),
                        existingLock.getExpiresAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    return LockResult.conflict(existingLock, message);
                }
            }
        }
        
        // Créer un nouveau lock
        EditLock newLock = new EditLock(resourceType, resourceId, user, lockScope, durationMinutes);
        editLockRepository.save(newLock);
        
        // Notifier les autres utilisateurs
        notifyLockAcquired(newLock);
        
        log.info("Lock acquis: {} pour ressource {}/{} par utilisateur {}", 
            newLock.getLockToken(), resourceType, resourceId, user.getUsername());
        
        return LockResult.success(newLock.getLockToken(), newLock.getExpiresAt());
    }
    
    // Vérifier si deux scopes peuvent coexister
    private boolean canCoexist(String existingScope, String newScope) {
        // Règles de coexistence
        if ("METADATA".equals(existingScope) && "BLOCK".equals(newScope)) return true;
        if ("BLOCK".equals(existingScope) && "METADATA".equals(newScope)) return true;
        if ("BLOCK".equals(existingScope) && "BLOCK".equals(newScope)) return true; // Différents blocs
        
        return false; // Par défaut, pas de coexistence
    }
    
    // Renouveler un lock (heartbeat)
    public boolean renewLock(String lockToken) {
        return renewLock(lockToken, LOCK_EXTENSION_MINUTES);
    }
    
    public boolean renewLock(String lockToken, int extensionMinutes) {
        Optional<EditLock> lockOpt = editLockRepository.findByLockToken(lockToken);
        
        if (lockOpt.isPresent()) {
            EditLock lock = lockOpt.get();
            
            if (lock.isExpired()) {
                // Lock expiré, le supprimer
                editLockRepository.delete(lock);
                return false;
            }
            
            lock.extend(extensionMinutes);
            editLockRepository.save(lock);
            
            log.debug("Lock renouvelé: {} jusqu'à {}", lockToken, lock.getExpiresAt());
            return true;
        }
        
        return false;
    }
    
    // Libérer un lock
    public boolean releaseLock(String lockToken) {
        Optional<EditLock> lockOpt = editLockRepository.findByLockToken(lockToken);
        
        if (lockOpt.isPresent()) {
            EditLock lock = lockOpt.get();
            editLockRepository.delete(lock);
            
            // Notifier la libération
            notifyLockReleased(lock);
            
            log.info("Lock libéré: {} pour ressource {}/{}", 
                lockToken, lock.getResourceType(), lock.getResourceId());
            return true;
        }
        
        return false;
    }
    
    // Libérer tous les locks d'un utilisateur
    public int releaseAllUserLocks(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + userId));
        
        List<EditLock> userLocks = editLockRepository.findActiveLocksForUser(user, LocalDateTime.now());
        
        for (EditLock lock : userLocks) {
            editLockRepository.delete(lock);
            notifyLockReleased(lock);
        }
        
        log.info("Libération de {} locks pour l'utilisateur {}", userLocks.size(), user.getUsername());
        return userLocks.size();
    }
    
    // Vérifier si un lock est valide
    public boolean isValidLock(String lockToken) {
        return editLockRepository.findByLockToken(lockToken)
            .map(lock -> !lock.isExpired())
            .orElse(false);
    }
    
    // Vérifier si un utilisateur peut modifier une ressource
    public boolean canUserModifyResource(ResourceType resourceType, Long resourceId, 
                                       Long userId, String requiredScope) {
        
        // Nettoyer les locks expirés
        editLockRepository.deleteExpiredLocks(LocalDateTime.now());
        
        List<EditLock> activeLocks = editLockRepository.findNonExpiredLocksForResource(
            resourceType, resourceId, LocalDateTime.now());
        
        // Si pas de locks, modification autorisée
        if (activeLocks.isEmpty()) {
            return true;
        }
        
        // Vérifier si l'utilisateur a un lock compatible
        for (EditLock lock : activeLocks) {
            if (lock.getLockedBy().getId().equals(userId)) {
                if (lock.getLockScope().equals(requiredScope) || 
                    canCoexist(lock.getLockScope(), requiredScope)) {
                    return true;
                }
            } else {
                // Lock d'un autre utilisateur
                if (!canCoexist(lock.getLockScope(), requiredScope)) {
                    return false;
                }
            }
        }
        
        return false;
    }
    
    // Obtenir les informations sur les locks d'une ressource
    public List<LockInfo> getResourceLocks(ResourceType resourceType, Long resourceId, Long currentUserId) {
        List<EditLock> locks = editLockRepository.findNonExpiredLocksForResource(
            resourceType, resourceId, LocalDateTime.now());
        
        return locks.stream()
            .map(lock -> new LockInfo(
                lock.getLockToken(),
                lock.getResourceType(),
                lock.getResourceId(),
                lock.getLockScope(),
                lock.getLockedBy().getUsername(),
                lock.getLockedAt(),
                lock.getExpiresAt(),
                lock.getLockedBy().getId().equals(currentUserId)
            ))
            .collect(Collectors.toList());
    }
    
    // Obtenir les locks actifs d'un utilisateur
    public List<LockInfo> getUserActiveLocks(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + userId));
        
        List<EditLock> locks = editLockRepository.findActiveLocksForUser(user, LocalDateTime.now());
        
        return locks.stream()
            .map(lock -> new LockInfo(
                lock.getLockToken(),
                lock.getResourceType(),
                lock.getResourceId(),
                lock.getLockScope(),
                lock.getLockedBy().getUsername(),
                lock.getLockedAt(),
                lock.getExpiresAt(),
                true
            ))
            .collect(Collectors.toList());
    }
    
    // Forcer la libération d'un lock (admin uniquement)
    public boolean forceReleaseLock(String lockToken, Long adminUserId) {
        // Vérifier que l'utilisateur est admin (à implémenter selon votre système)
        
        Optional<EditLock> lockOpt = editLockRepository.findByLockToken(lockToken);
        
        if (lockOpt.isPresent()) {
            EditLock lock = lockOpt.get();
            editLockRepository.delete(lock);
            
            // Notifier la libération forcée
            notifyLockForcedRelease(lock, adminUserId);
            
            log.warn("Lock forcé libéré par admin {}: {} pour ressource {}/{}", 
                adminUserId, lockToken, lock.getResourceType(), lock.getResourceId());
            return true;
        }
        
        return false;
    }
    
    // Notifications WebSocket
    private void notifyLockAcquired(EditLock lock) {
        Map<String, Object> notification = Map.of(
            "type", "LOCK_ACQUIRED",
            "resourceType", lock.getResourceType(),
            "resourceId", lock.getResourceId(),
            "lockScope", lock.getLockScope(),
            "lockedBy", lock.getLockedBy().getUsername(),
            "expiresAt", lock.getExpiresAt(),
            "lockToken", lock.getLockToken()
        );
        
        // Broadcast à tous les utilisateurs intéressés par cette ressource
        messagingTemplate.convertAndSend(
            "/topic/locks/" + lock.getResourceType() + "/" + lock.getResourceId(), 
            notification
        );
    }
    
    private void notifyLockReleased(EditLock lock) {
        Map<String, Object> notification = Map.of(
            "type", "LOCK_RELEASED",
            "resourceType", lock.getResourceType(),
            "resourceId", lock.getResourceId(),
            "lockScope", lock.getLockScope(),
            "lockToken", lock.getLockToken()
        );
        
        messagingTemplate.convertAndSend(
            "/topic/locks/" + lock.getResourceType() + "/" + lock.getResourceId(), 
            notification
        );
    }
    
    private void notifyLockRenewed(EditLock lock) {
        Map<String, Object> notification = Map.of(
            "type", "LOCK_RENEWED",
            "lockToken", lock.getLockToken(),
            "newExpiresAt", lock.getExpiresAt()
        );
        
        // Notifier directement l'utilisateur qui possède le lock
        messagingTemplate.convertAndSendToUser(
            lock.getLockedBy().getId().toString(),
            "/queue/lock-updates",
            notification
        );
    }
    
    private void notifyLockForcedRelease(EditLock lock, Long adminUserId) {
        Map<String, Object> notification = Map.of(
            "type", "LOCK_FORCED_RELEASE",
            "resourceType", lock.getResourceType(),
            "resourceId", lock.getResourceId(),
            "lockToken", lock.getLockToken(),
            "releasedBy", adminUserId
        );
        
        // Notifier l'utilisateur qui avait le lock
        messagingTemplate.convertAndSendToUser(
            lock.getLockedBy().getId().toString(),
            "/queue/lock-updates",
            notification
        );
        
        // Broadcast aux autres utilisateurs
        messagingTemplate.convertAndSend(
            "/topic/locks/" + lock.getResourceType() + "/" + lock.getResourceId(), 
            notification
        );
    }
}
