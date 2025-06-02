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

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.dto.conflict.ConflictDTO;
import com.ipi.mesi_backend_rpg.dto.conflict.ConflictResolutionRequestDTO;
import com.ipi.mesi_backend_rpg.service.ConflictResolutionService;
import com.ipi.mesi_backend_rpg.service.MergeService;
import com.ipi.mesi_backend_rpg.utils.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/conflicts")
@RequiredArgsConstructor
@Slf4j
public class ConflictController {
    
    private final MergeService mergeService;
    private final ConflictResolutionService conflictResolutionService;
    private final SecurityUtils securityUtils;
    
    /**
     * Récupère un conflit spécifique
     */
    @GetMapping("/{conflictId}")
    public ResponseEntity<ConflictDTO> getConflict(@PathVariable String conflictId) {
        ConflictDTO conflict = conflictResolutionService.getConflict(conflictId);
        
        if (conflict == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(conflict);
    }
    
    /**
     * Récupère tous les conflits actifs pour l'utilisateur connecté
     */
    @GetMapping("/active")
    public ResponseEntity<List<ConflictDTO>> getActiveConflicts(HttpServletRequest request) {
        Long userId = securityUtils.getCurrentUserIdFromRequest(request);
        List<ConflictDTO> conflicts = conflictResolutionService.getActiveConflictsForUser(userId);
        return ResponseEntity.ok(conflicts);
    }
    
    /**
     * Résout un conflit avec une stratégie manuelle
     */
    @PostMapping("/{conflictId}/resolve")
    public ResponseEntity<Map<String, Object>> resolveConflict(
            @PathVariable String conflictId,
            @Valid @RequestBody ConflictResolutionRequestDTO resolutionRequest) {
        
        try {
            // Vérifier que l'ID du conflit correspond
            if (!conflictId.equals(resolutionRequest.getConflictId())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "L'ID du conflit ne correspond pas"));
            }
            
            MergeService.MergeResult result = mergeService.resolveConflict(resolutionRequest);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Conflit résolu avec succès",
                    "data", result.getData()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                        "success", false,
                        "error", result.getErrorMessage() != null ? result.getErrorMessage() : "Erreur inconnue"
                    ));
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la résolution du conflit {}", conflictId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Erreur serveur lors de la résolution"
                ));
        }
    }
    
    /**
     * Résolution automatique d'un conflit
     */
    @PostMapping("/{conflictId}/auto-resolve")
    public ResponseEntity<Map<String, Object>> autoResolveConflict(
            @PathVariable String conflictId,
            @RequestParam(defaultValue = "take_user") String strategy) {
        
        try {
            MergeService.MergeResult result = mergeService.autoResolveConflict(conflictId, strategy);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Conflit résolu automatiquement",
                    "strategy", strategy,
                    "data", result.getData()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                        "success", false,
                        "error", result.getErrorMessage() != null ? result.getErrorMessage() : "Erreur inconnue"
                    ));
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la résolution automatique du conflit {}", conflictId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Erreur serveur lors de la résolution automatique"
                ));
        }
    }
    
    /**
     * Annule un conflit (l'utilisateur abandonne ses modifications)
     */
    @DeleteMapping("/{conflictId}")
    public ResponseEntity<Map<String, Object>> cancelConflict(@PathVariable String conflictId) {
        try {
            conflictResolutionService.cancelConflict(conflictId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Conflit annulé avec succès"
            ));
            
        } catch (Exception e) {
            log.error("Erreur lors de l'annulation du conflit {}", conflictId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Erreur serveur lors de l'annulation"
                ));
        }
    }
    
    /**
     * Endpoint pour tester la mise à jour avec merge intelligent d'une ModuleVersion
     */
    @PutMapping("/module-version/{id}/smart-update")
    public ResponseEntity<Map<String, Object>> smartUpdateModuleVersion(
            @PathVariable Long id,
            @Valid @RequestBody ModuleVersionDTO moduleVersionDTO) {
        
        try {
            MergeService.MergeResult result = mergeService.updateModuleVersionWithMerge(id, moduleVersionDTO);
            
            switch (result.getType()) {
                case SUCCESS:
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Mise à jour réussie",
                        "data", result.getData()
                    ));
                    
                case CONFLICT:
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                            "success", false,
                            "type", "conflict",
                            "message", "Conflit détecté, résolution requise",
                            "conflict", result.getConflict()
                        ));
                        
                case ERROR:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                            "success", false,
                            "type", "error",
                            "error", result.getErrorMessage()
                        ));
                        
                default:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                            "success", false,
                            "error", "Type de résultat inconnu"
                        ));
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour intelligente de ModuleVersion {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Erreur serveur lors de la mise à jour"
                ));
        }
    }
    
    /**
     * Endpoint pour tester la mise à jour avec merge intelligent d'un Block
     */
    @PutMapping("/block/{id}/smart-update")
    public ResponseEntity<Map<String, Object>> smartUpdateBlock(
            @PathVariable Long id,
            @Valid @RequestBody BlockDTO blockDTO) {
        
        try {
            MergeService.MergeResult result = mergeService.updateBlockWithMerge(id, blockDTO);
            
            switch (result.getType()) {
                case SUCCESS:
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Mise à jour réussie",
                        "data", result.getData()
                    ));
                    
                case CONFLICT:
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(
                            "success", false,
                            "type", "conflict",
                            "message", "Conflit détecté, résolution requise",
                            "conflict", result.getConflict()
                        ));
                        
                case ERROR:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                            "success", false,
                            "type", "error",
                            "error", result.getErrorMessage()
                        ));
                        
                default:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                            "success", false,
                            "error", "Type de résultat inconnu"
                        ));
            }
            
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour intelligente de Block {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Erreur serveur lors de la mise à jour"
                ));
        }
    }
    
    /**
     * Obtient des suggestions de résolution pour un conflit
     */
    @GetMapping("/{conflictId}/suggestions")
    public ResponseEntity<Map<String, Object>> getResolutionSuggestions(@PathVariable String conflictId) {
        ConflictDTO conflict = conflictResolutionService.getConflict(conflictId);
        
        if (conflict == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Générer des suggestions basées sur le type de conflit
        Map<String, Object> suggestions = Map.of(
            "autoResolveOptions", List.of(
                Map.of("strategy", "take_current", "description", "Garder la version actuelle"),
                Map.of("strategy", "take_user", "description", "Prendre vos modifications"),
                Map.of("strategy", "auto_merge", "description", "Fusion automatique intelligente")
            ),
            "recommendedStrategy", getRecommendedStrategy(conflict),
            "conflictComplexity", calculateConflictComplexity(conflict)
        );
        
        return ResponseEntity.ok(suggestions);
    }
    
    /**
     * Calcule la stratégie recommandée
     */
    private String getRecommendedStrategy(ConflictDTO conflict) {
        // Logique simple pour recommander une stratégie
        if (conflict.getFieldConflicts().size() <= 2) {
            return "auto_merge";
        } else if (conflict.getFieldConflicts().stream().allMatch(f -> f.isCanAutoResolve())) {
            return "auto_merge";
        } else {
            return "manual";
        }
    }
    
    /**
     * Calcule la complexité du conflit
     */
    private String calculateConflictComplexity(ConflictDTO conflict) {
        int conflictCount = conflict.getFieldConflicts().size();
        boolean hasBlockConflicts = conflict.getFieldConflicts().stream()
            .anyMatch(f -> f.getFieldName().contains("block"));
        
        if (conflictCount <= 2 && !hasBlockConflicts) {
            return "simple";
        } else if (conflictCount <= 5) {
            return "moderate";
        } else {
            return "complex";
        }
    }
}