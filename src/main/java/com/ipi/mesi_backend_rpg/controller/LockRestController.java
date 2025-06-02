package com.ipi.mesi_backend_rpg.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ipi.mesi_backend_rpg.dto.lock.LockAcquisitionRequestDTO;
import com.ipi.mesi_backend_rpg.dto.lock.LockValidationRequestDTO;
import com.ipi.mesi_backend_rpg.dto.lock.ResourcePermissionCheckRequestDTO;
import com.ipi.mesi_backend_rpg.model.ResourceType;
import com.ipi.mesi_backend_rpg.service.EditLockService;
import com.ipi.mesi_backend_rpg.utils.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/locks")
@RequiredArgsConstructor
public class LockRestController {
    private final EditLockService lockService;
    private final SecurityUtils securityUtils;
    
    @PostMapping("/acquire")
    public ResponseEntity<EditLockService.LockResult> acquireLock(
            @RequestBody LockAcquisitionRequestDTO request,
            HttpServletRequest httpRequest) {
        
        Long userId = securityUtils.getCurrentUserIdFromRequest(httpRequest);
        
        EditLockService.LockResult result = lockService.acquireLock(
            request.getResourceType(),
            request.getResourceId(),
            userId,
            request.getLockScope() != null ? request.getLockScope() : "FULL",
            request.getDurationMinutes() != null ? request.getDurationMinutes() : 10
        );
        
        if (result.isAcquired()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
    }
    
    @DeleteMapping("/{lockToken}")
    public ResponseEntity<Map<String, Object>> releaseLock(@PathVariable String lockToken) {
        boolean released = lockService.releaseLock(lockToken);
        
        return ResponseEntity.ok(Map.of(
            "released", released,
            "lockToken", lockToken,
            "message", released ? "Lock libéré avec succès" : "Lock non trouvé ou déjà libéré"
        ));
    }
    
    @PutMapping("/{lockToken}/renew")
    public ResponseEntity<Map<String, Object>> renewLock(@PathVariable String lockToken,
                                                        @RequestParam(defaultValue = "5") int extensionMinutes) {
        boolean renewed = lockService.renewLock(lockToken, extensionMinutes);
        
        if (renewed) {
            return ResponseEntity.ok(Map.of(
                "renewed", true, 
                "lockToken", lockToken,
                "message", "Lock renouvelé avec succès"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.GONE)
                .body(Map.of(
                    "renewed", false, 
                    "message", "Lock expiré ou introuvable",
                    "lockToken", lockToken
                ));
        }
    }
    
    @GetMapping("/user/me")
    public ResponseEntity<List<EditLockService.LockInfo>> getMyLocks(HttpServletRequest httpRequest) {
        Long userId = securityUtils.getCurrentUserIdFromRequest(httpRequest);
        List<EditLockService.LockInfo> locks = lockService.getUserActiveLocks(userId);
        return ResponseEntity.ok(locks);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EditLockService.LockInfo>> getUserLocks(@PathVariable Long userId,
                                                                      HttpServletRequest httpRequest) {
        // Vérifier les droits (seulement ses propres locks ou admin)
        Long currentUserId = securityUtils.getCurrentUserIdFromRequest(httpRequest);
        
        if (!currentUserId.equals(userId) && !securityUtils.hasRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<EditLockService.LockInfo> locks = lockService.getUserActiveLocks(userId);
        return ResponseEntity.ok(locks);
    }
    
    @GetMapping("/resource/{resourceType}/{resourceId}")
    public ResponseEntity<List<EditLockService.LockInfo>> getResourceLocks(
            @PathVariable ResourceType resourceType,
            @PathVariable Long resourceId,
            HttpServletRequest httpRequest) {
        
        Long userId = securityUtils.getCurrentUserIdFromRequest(httpRequest);
        List<EditLockService.LockInfo> locks = lockService.getResourceLocks(resourceType, resourceId, userId);
        return ResponseEntity.ok(locks);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateLock(@RequestBody LockValidationRequestDTO request) {
        boolean valid = lockService.isValidLock(request.getLockToken());
        
        return ResponseEntity.ok(Map.of(
            "valid", valid,
            "lockToken", request.getLockToken(),
            "message", valid ? "Lock valide" : "Lock invalide ou expiré"
        ));
    }
    
    @PostMapping("/check-permission")
    public ResponseEntity<Map<String, Object>> checkModificationPermission(
            @RequestBody ResourcePermissionCheckRequestDTO request,
            HttpServletRequest httpRequest) {
        
        Long userId = securityUtils.getCurrentUserIdFromRequest(httpRequest);
        
        boolean canModify = lockService.canUserModifyResource(
            request.getResourceType(),
            request.getResourceId(),
            userId,
            request.getRequiredScope() != null ? request.getRequiredScope() : "FULL"
        );
        
        List<EditLockService.LockInfo> existingLocks = lockService.getResourceLocks(
            request.getResourceType(), request.getResourceId(), userId);
        
        return ResponseEntity.ok(Map.of(
            "canModify", canModify,
            "userId", userId,
            "resourceType", request.getResourceType(),
            "resourceId", request.getResourceId(),
            "existingLocks", existingLocks,
            "lockCount", existingLocks.size()
        ));
    }
    
    // Admin endpoints
    @DeleteMapping("/admin/force-release/{lockToken}")
    public ResponseEntity<Map<String, Object>> forceReleaseLock(@PathVariable String lockToken,
                                                               HttpServletRequest httpRequest) {
        Long adminUserId = securityUtils.getCurrentUserIdFromRequest(httpRequest);
        
        // Vérifier les droits admin
        if (!securityUtils.hasRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Droits insuffisants"));
        }
        
        boolean released = lockService.forceReleaseLock(lockToken, adminUserId);
        
        return ResponseEntity.ok(Map.of(
            "forcedRelease", released,
            "lockToken", lockToken,
            "releasedBy", adminUserId,
            "message", released ? "Lock forcé libéré par admin" : "Échec de la libération forcée"
        ));
    }
    
    @DeleteMapping("/admin/user/{userId}/release-all")
    public ResponseEntity<Map<String, Object>> releaseAllUserLocks(@PathVariable Long userId,
                                                                  HttpServletRequest httpRequest) {
        Long adminUserId = securityUtils.getCurrentUserIdFromRequest(httpRequest);
        
        // Vérifier les droits admin
        if (!securityUtils.hasRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Droits insuffisants"));
        }
        
        int releasedCount = lockService.releaseAllUserLocks(userId);
        
        return ResponseEntity.ok(Map.of(
            "releasedCount", releasedCount,
            "userId", userId,
            "releasedBy", adminUserId,
            "message", releasedCount + " locks libérés"
        ));
    }
}
