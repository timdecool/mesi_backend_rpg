package com.ipi.mesi_backend_rpg.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.dto.conflict.ConflictDTO;
import com.ipi.mesi_backend_rpg.dto.conflict.ConflictResolutionRequestDTO;
import com.ipi.mesi_backend_rpg.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MergeService {
    
    private final ConflictDetectionService conflictDetectionService;
    private final ConflictResolutionService conflictResolutionService;
    private final ModuleVersionService moduleVersionService;
    private final BlockService blockService;
    private final SecurityUtils securityUtils;
    
    /**
     * Tentative de mise à jour d'une ModuleVersion avec gestion intelligente des conflits
     */
    public MergeResult updateModuleVersionWithMerge(Long id, ModuleVersionDTO moduleVersionDTO) {
        Long userId = securityUtils.getCurrentUserId();
        
        try {
            // Tentative de mise à jour normale
            ModuleVersionDTO updated = moduleVersionService.updateVersion(moduleVersionDTO, id);
            return MergeResult.success(updated);
            
        } catch (OptimisticLockingFailureException | ResponseStatusException e) {
            // En cas de conflit, détecter et proposer une résolution
            log.info("Conflit détecté lors de la mise à jour de ModuleVersion {}, démarrage du processus de merge", id);
            
            return handleModuleVersionConflict(id, moduleVersionDTO, userId);
        }
    }
    
    /**
     * Tentative de mise à jour d'un Block avec gestion intelligente des conflits
     */
    public MergeResult updateBlockWithMerge(Long id, BlockDTO blockDTO) {
        Long userId = securityUtils.getCurrentUserId();
        
        try {
            // Tentative de mise à jour normale
            BlockDTO updated = blockService.updateBlock(id, blockDTO);
            return MergeResult.success(updated);
            
        } catch (OptimisticLockingFailureException | ResponseStatusException e) {
            log.info("Conflit détecté lors de la mise à jour de Block {}, démarrage du processus de merge", id);
            
            return handleBlockConflict(id, blockDTO, userId);
        }
    }
    
    /**
     * Résout un conflit avec la stratégie spécifiée
     */
    public MergeResult resolveConflict(ConflictResolutionRequestDTO resolutionRequest) {
        try {
            Object resolvedObject = conflictResolutionService.resolveConflict(resolutionRequest);
            
            ConflictDTO conflict = conflictResolutionService.getConflict(resolutionRequest.getConflictId());
            if (conflict == null) {
                throw new IllegalArgumentException("Conflit non trouvé");
            }
            
            // Appliquer la résolution selon le type de ressource
            switch (conflict.getResourceType()) {
                case MODULE_VERSION:
                    ModuleVersionDTO resolvedVersion = (ModuleVersionDTO) resolvedObject;
                    ModuleVersionDTO finalVersion = moduleVersionService.updateVersion(resolvedVersion, resolvedVersion.id());
                    return MergeResult.success(finalVersion);
                    
                case BLOCK:
                    BlockDTO resolvedBlock = (BlockDTO) resolvedObject;
                    BlockDTO finalBlock = blockService.updateBlock(resolvedBlock.getId(), resolvedBlock);
                    return MergeResult.success(finalBlock);
                    
                default:
                    throw new UnsupportedOperationException("Type de ressource non supporté");
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la résolution du conflit {}", resolutionRequest.getConflictId(), e);
            return MergeResult.error("Erreur lors de la résolution: " + e.getMessage());
        }
    }
    
    /**
     * Résolution automatique d'un conflit (pour les conflits simples)
     */
    public MergeResult autoResolveConflict(String conflictId, String strategy) {
        ConflictDTO conflict = conflictResolutionService.getConflict(conflictId);
        if (conflict == null) {
            throw new IllegalArgumentException("Conflit non trouvé: " + conflictId);
        }
        
        // Créer une demande de résolution automatique
        ConflictResolutionRequestDTO autoRequest = createAutoResolutionRequest(conflict, strategy);
        
        return resolveConflict(autoRequest);
    }
    
    /**
     * Gère un conflit de ModuleVersion
     */
    private MergeResult handleModuleVersionConflict(Long id, ModuleVersionDTO userVersion, Long userId) {
        try {
            // Récupérer la version actuelle en base
            ModuleVersionDTO currentVersion = moduleVersionService.findById(id);
            
            // Détecter les conflits
            ConflictDTO conflict = conflictDetectionService.detectModuleVersionConflicts(
                userVersion, currentVersion, userId);
            
            if (conflict == null) {
                // Pas de conflit réel, réessayer la mise à jour
                ModuleVersionDTO updated = moduleVersionService.updateVersion(userVersion, id);
                return MergeResult.success(updated);
            }
            
            // Stocker les données dans les métadonnées du conflit
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("currentVersion", currentVersion);
            metadata.put("userVersion", userVersion);
            conflict.setMetadata(metadata);
            
            // Stocker le conflit pour résolution
            conflictResolutionService.storeConflict(conflict);
            
            return MergeResult.conflict(conflict);
            
        } catch (Exception e) {
            log.error("Erreur lors de la gestion du conflit ModuleVersion", e);
            return MergeResult.error("Erreur lors de la détection du conflit: " + e.getMessage());
        }
    }
    
    /**
     * Gère un conflit de Block
     */
    private MergeResult handleBlockConflict(Long id, BlockDTO userBlock, Long userId) {
        try {
            // Récupérer le bloc actuel en base (simulation - vous devrez adapter)
            BlockDTO currentBlock = getCurrentBlock(id);
            
            // Détecter les conflits
            ConflictDTO conflict = conflictDetectionService.detectBlockConflicts(
                userBlock, currentBlock, userId);
            
            if (conflict == null) {
                // Pas de conflit réel, réessayer la mise à jour
                BlockDTO updated = blockService.updateBlock(id, userBlock);
                return MergeResult.success(updated);
            }
            
            // Stocker les données dans les métadonnées du conflit
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("currentBlock", currentBlock);
            metadata.put("userBlock", userBlock);
            conflict.setMetadata(metadata);
            
            // Stocker le conflit pour résolution
            conflictResolutionService.storeConflict(conflict);
            
            return MergeResult.conflict(conflict);
            
        } catch (Exception e) {
            log.error("Erreur lors de la gestion du conflit Block", e);
            return MergeResult.error("Erreur lors de la détection du conflit: " + e.getMessage());
        }
    }
    
    /**
     * Récupère le bloc actuel (à adapter selon votre implémentation)
     */
    private BlockDTO getCurrentBlock(Long id) {
        // Implémentation simplifiée - vous devrez adapter selon votre architecture
        // En réalité, vous devriez récupérer le bloc via le BlockService
        throw new UnsupportedOperationException("À implémenter selon votre architecture");
    }
    
    /**
     * Crée une demande de résolution automatique
     */
    private ConflictResolutionRequestDTO createAutoResolutionRequest(ConflictDTO conflict, String strategy) {
        ConflictResolutionRequestDTO request = new ConflictResolutionRequestDTO();
        request.setConflictId(conflict.getConflictId());
        request.setResolutionStrategy("auto");
        
        // Créer les résolutions pour chaque champ selon la stratégie
        conflict.getFieldConflicts().forEach(fieldConflict -> {
            ConflictResolutionRequestDTO.FieldResolutionDTO fieldResolution = 
                new ConflictResolutionRequestDTO.FieldResolutionDTO();
            fieldResolution.setFieldName(fieldConflict.getFieldName());
            
            // Appliquer la stratégie
            switch (strategy) {
                case "take_current":
                    fieldResolution.setResolution("current");
                    break;
                case "take_user":
                    fieldResolution.setResolution("user");
                    break;
                case "auto_merge":
                    // Utiliser la suggestion automatique si disponible
                    fieldResolution.setResolution(fieldConflict.getSuggestedResolution() != null ? 
                        fieldConflict.getSuggestedResolution() : "user");
                    break;
                default:
                    fieldResolution.setResolution("user");
            }
            
            request.getFieldResolutions().add(fieldResolution);
        });
        
        return request;
    }
    
    /**
     * Classe de résultat pour les opérations de merge
     */
    public static class MergeResult {
        private final boolean success;
        private final Object data;
        private final ConflictDTO conflict;
        private final String errorMessage;
        private final MergeResultType type;
        
        private MergeResult(boolean success, Object data, ConflictDTO conflict, 
                           String errorMessage, MergeResultType type) {
            this.success = success;
            this.data = data;
            this.conflict = conflict;
            this.errorMessage = errorMessage;
            this.type = type;
        }
        
        public static MergeResult success(Object data) {
            return new MergeResult(true, data, null, null, MergeResultType.SUCCESS);
        }
        
        public static MergeResult conflict(ConflictDTO conflict) {
            return new MergeResult(false, null, conflict, null, MergeResultType.CONFLICT);
        }
        
        public static MergeResult error(String errorMessage) {
            return new MergeResult(false, null, null, errorMessage, MergeResultType.ERROR);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public Object getData() { return data; }
        public ConflictDTO getConflict() { return conflict; }
        public String getErrorMessage() { return errorMessage; }
        public MergeResultType getType() { return type; }
        
        public enum MergeResultType {
            SUCCESS,
            CONFLICT,
            ERROR
        }
    }
}