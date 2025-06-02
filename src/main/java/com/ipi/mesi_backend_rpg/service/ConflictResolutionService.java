package com.ipi.mesi_backend_rpg.service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.dto.conflict.ConflictDTO;
import com.ipi.mesi_backend_rpg.dto.conflict.ConflictResolutionRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConflictResolutionService {
    
    // Cache temporaire pour stocker les conflits en cours de résolution
    private final Map<String, ConflictDTO> activeConflicts = new ConcurrentHashMap<>();
    
    /**
     * Stocke un conflit pour résolution ultérieure
     */
    public void storeConflict(ConflictDTO conflict) {
        activeConflicts.put(conflict.getConflictId(), conflict);
        
        // Nettoyage automatique après 30 minutes
        scheduleConflictCleanup(conflict.getConflictId(), 30 * 60 * 1000);
    }
    
    /**
     * Récupère un conflit stocké
     */
    public ConflictDTO getConflict(String conflictId) {
        return activeConflicts.get(conflictId);
    }
    
    /**
     * Résout un conflit selon la stratégie spécifiée
     */
    public Object resolveConflict(ConflictResolutionRequestDTO resolutionRequest) {
        ConflictDTO conflict = activeConflicts.get(resolutionRequest.getConflictId());
        if (conflict == null) {
            throw new IllegalArgumentException("Conflit non trouvé: " + resolutionRequest.getConflictId());
        }
        
        try {
            Object resolvedObject = null;
            
            switch (conflict.getResourceType()) {
                case MODULE_VERSION:
                    resolvedObject = resolveModuleVersionConflict(conflict, resolutionRequest);
                    break;
                case BLOCK:
                    resolvedObject = resolveBlockConflict(conflict, resolutionRequest);
                    break;
                default:
                    throw new UnsupportedOperationException("Type de ressource non supporté: " + conflict.getResourceType());
            }
            
            // Marquer le conflit comme résolu
            conflict.setStatus(ConflictDTO.ConflictStatus.RESOLVED);
            
            // Nettoyer le conflit du cache après résolution
            activeConflicts.remove(resolutionRequest.getConflictId());
            
            log.info("Conflit {} résolu avec succès", resolutionRequest.getConflictId());
            return resolvedObject;
            
        } catch (Exception e) {
            log.error("Erreur lors de la résolution du conflit {}", resolutionRequest.getConflictId(), e);
            throw new RuntimeException("Erreur lors de la résolution du conflit", e);
        }
    }
    
    /**
     * Résout un conflit de ModuleVersion
     */
    private ModuleVersionDTO resolveModuleVersionConflict(ConflictDTO conflict, 
                                                         ConflictResolutionRequestDTO resolutionRequest) {
        // Récupérer la version courante et la version utilisateur depuis les métadonnées du conflit
        ModuleVersionDTO currentVersion = (ModuleVersionDTO) conflict.getMetadata().get("currentVersion");
        ModuleVersionDTO userVersion = (ModuleVersionDTO) conflict.getMetadata().get("userVersion");
        
        if (currentVersion == null || userVersion == null) {
            throw new IllegalStateException("Données de conflit incomplètes");
        }
        
        // Créer une version fusionnée
        ModuleVersionDTO resolvedVersion = createMergedModuleVersion(currentVersion, userVersion, resolutionRequest);
        
        return resolvedVersion;
    }
    
    /**
     * Résout un conflit de Block
     */
    private BlockDTO resolveBlockConflict(ConflictDTO conflict, 
                                        ConflictResolutionRequestDTO resolutionRequest) {
        BlockDTO currentBlock = (BlockDTO) conflict.getMetadata().get("currentBlock");
        BlockDTO userBlock = (BlockDTO) conflict.getMetadata().get("userBlock");
        
        if (currentBlock == null || userBlock == null) {
            throw new IllegalStateException("Données de conflit incomplètes");
        }
        
        return createMergedBlock(currentBlock, userBlock, resolutionRequest);
    }
    
    /**
     * Crée une version fusionnée d'un ModuleVersion
     */
    private ModuleVersionDTO createMergedModuleVersion(ModuleVersionDTO currentVersion, 
                                                      ModuleVersionDTO userVersion,
                                                      ConflictResolutionRequestDTO resolutionRequest) {
        try {
            // Commencer avec la version courante comme base
            ModuleVersionDTO mergedVersion = cloneModuleVersionDTO(currentVersion);
            
            // Appliquer les résolutions champ par champ
            for (ConflictResolutionRequestDTO.FieldResolutionDTO fieldResolution : resolutionRequest.getFieldResolutions()) {
                applyFieldResolution(mergedVersion, userVersion, fieldResolution);
            }
            
            // Fusionner les blocs si nécessaire
            if (hasBlockConflicts(resolutionRequest)) {
                List<BlockDTO> mergedBlocks = mergeBlocks(currentVersion.blocks(), userVersion.blocks(), resolutionRequest);
                mergedVersion = new ModuleVersionDTO(
                    mergedVersion.id(),
                    mergedVersion.moduleId(),
                    mergedVersion.version(),
                    mergedVersion.creator(),
                    mergedVersion.createdAt(),
                    LocalDateTime.now(), // updatedAt
                    mergedVersion.published(),
                    mergedVersion.gameSystemId(),
                    mergedVersion.language(),
                    mergedBlocks,
                    mergedVersion.entityVersion(),
                    LocalDateTime.now() // lastModified
                );
            }
            
            return mergedVersion;
            
        } catch (Exception e) {
            log.error("Erreur lors de la fusion de ModuleVersion", e);
            throw new RuntimeException("Erreur lors de la fusion", e);
        }
    }
    
    /**
     * Crée un bloc fusionné
     */
    private BlockDTO createMergedBlock(BlockDTO currentBlock, BlockDTO userBlock,
                                     ConflictResolutionRequestDTO resolutionRequest) {
        try {
            // Cloner le bloc courant comme base
            BlockDTO mergedBlock = cloneBlockDTO(currentBlock);
            
            // Appliquer les résolutions champ par champ
            for (ConflictResolutionRequestDTO.FieldResolutionDTO fieldResolution : resolutionRequest.getFieldResolutions()) {
                applyBlockFieldResolution(mergedBlock, userBlock, fieldResolution);
            }
            
            return mergedBlock;
            
        } catch (Exception e) {
            log.error("Erreur lors de la fusion de Block", e);
            throw new RuntimeException("Erreur lors de la fusion", e);
        }
    }
    
    /**
     * Applique une résolution de champ à un objet
     */
    private void applyFieldResolution(Object targetObject, Object sourceObject,
                                    ConflictResolutionRequestDTO.FieldResolutionDTO fieldResolution) {
        try {
            Field field = targetObject.getClass().getDeclaredField(fieldResolution.getFieldName());
            field.setAccessible(true);
            
            Object valueToApply = null;
            
            switch (fieldResolution.getResolution()) {
                case "current":
                    // Garder la valeur actuelle (ne rien faire)
                    return;
                case "user":
                    valueToApply = field.get(sourceObject);
                    break;
                case "custom":
                    valueToApply = fieldResolution.getCustomValue();
                    break;
                default:
                    log.warn("Stratégie de résolution inconnue: {}", fieldResolution.getResolution());
                    return;
            }
            
            field.set(targetObject, valueToApply);
            log.debug("Champ {} résolu avec la stratégie {}", fieldResolution.getFieldName(), fieldResolution.getResolution());
            
        } catch (Exception e) {
            log.error("Erreur lors de l'application de la résolution pour le champ {}", fieldResolution.getFieldName(), e);
        }
    }
    
    /**
     * Applique une résolution de champ spécifique aux blocs
     */
    private void applyBlockFieldResolution(BlockDTO targetBlock, BlockDTO sourceBlock,
                                         ConflictResolutionRequestDTO.FieldResolutionDTO fieldResolution) {
        // Implémentation spécialisée pour les blocs
        applyFieldResolution(targetBlock, sourceBlock, fieldResolution);
    }
    
    /**
     * Fusionne les listes de blocs
     */
    private List<BlockDTO> mergeBlocks(List<BlockDTO> currentBlocks, List<BlockDTO> userBlocks,
                                     ConflictResolutionRequestDTO resolutionRequest) {
        // Stratégie simple : prendre les blocs utilisateur par défaut
        // À adapter selon vos besoins spécifiques
        
        List<BlockDTO> mergedBlocks = new ArrayList<>();
        
        // Trouver la résolution pour l'ordre des blocs
        ConflictResolutionRequestDTO.FieldResolutionDTO blockOrderResolution = 
            resolutionRequest.getFieldResolutions().stream()
                .filter(r -> "blockOrder".equals(r.getFieldName()))
                .findFirst()
                .orElse(null);
        
        if (blockOrderResolution != null) {
            switch (blockOrderResolution.getResolution()) {
                case "current":
                    mergedBlocks.addAll(currentBlocks);
                    break;
                case "user":
                    mergedBlocks.addAll(userBlocks);
                    break;
                case "custom":
                    // Logique personnalisée basée sur blockOrderResolution.getOptions()
                    mergedBlocks.addAll(mergeBlocksCustom(currentBlocks, userBlocks, blockOrderResolution));
                    break;
                default:
                    mergedBlocks.addAll(userBlocks); // Par défaut
            }
        } else {
            mergedBlocks.addAll(userBlocks); // Par défaut
        }
        
        return mergedBlocks;
    }
    
    /**
     * Fusion personnalisée de blocs
     */
    private List<BlockDTO> mergeBlocksCustom(List<BlockDTO> currentBlocks, List<BlockDTO> userBlocks,
                                           ConflictResolutionRequestDTO.FieldResolutionDTO resolution) {
        // Implémentation d'une logique de fusion personnalisée
        // Peut être étendue selon vos besoins
        return new ArrayList<>(userBlocks);
    }
    
    /**
     * Méthodes utilitaires de clonage
     */
    private ModuleVersionDTO cloneModuleVersionDTO(ModuleVersionDTO original) {
        return new ModuleVersionDTO(
            original.id(),
            original.moduleId(),
            original.version(),
            original.creator(),
            original.createdAt(),
            original.updatedAt(),
            original.published(),
            original.gameSystemId(),
            original.language(),
            new ArrayList<>(original.blocks()),
            original.entityVersion(),
            original.lastModified()
        );
    }
    
    private BlockDTO cloneBlockDTO(BlockDTO original) {
        // Implémentation simplifiée - à adapter selon vos types de blocs
        try {
            return (BlockDTO) original.getClass()
                .getConstructor()
                .newInstance();
        } catch (Exception e) {
            log.error("Erreur lors du clonage de BlockDTO", e);
            return original;
        }
    }
    
    /**
     * Vérifie s'il y a des conflits de blocs
     */
    private boolean hasBlockConflicts(ConflictResolutionRequestDTO resolutionRequest) {
        return resolutionRequest.getFieldResolutions().stream()
                .anyMatch(r -> r.getFieldName().contains("block"));
    }
    
    /**
     * Nettoie automatiquement les conflits expirés
     */
    private void scheduleConflictCleanup(String conflictId, long delayMillis) {
        // Implémentation simple avec Thread - à remplacer par un scheduler en production
        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                ConflictDTO removed = activeConflicts.remove(conflictId);
                if (removed != null) {
                    log.info("Conflit {} nettoyé automatiquement après expiration", conflictId);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * Annule un conflit
     */
    public void cancelConflict(String conflictId) {
        ConflictDTO conflict = activeConflicts.remove(conflictId);
        if (conflict != null) {
            conflict.setStatus(ConflictDTO.ConflictStatus.CANCELLED);
            log.info("Conflit {} annulé", conflictId);
        }
    }
    
    /**
     * Obtient tous les conflits actifs pour un utilisateur
     */
    public List<ConflictDTO> getActiveConflictsForUser(Long userId) {
        return activeConflicts.values().stream()
                .filter(c -> userId.equals(c.getCurrentUserId()))
                .toList();
    }
}