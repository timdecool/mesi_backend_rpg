package com.ipi.mesi_backend_rpg.service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.dto.conflict.ConflictDTO;
import com.ipi.mesi_backend_rpg.dto.conflict.FieldConflictDTO;
import com.ipi.mesi_backend_rpg.model.ConflictType;
import com.ipi.mesi_backend_rpg.model.ResourceType;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConflictDetectionService {
    
    private final UserRepository userRepository;
    
    /**
     * Détecte les conflits entre une version utilisateur et la version actuelle en BD
     */
    public ConflictDTO detectModuleVersionConflicts(ModuleVersionDTO userVersion, 
                                                    ModuleVersionDTO currentVersion,
                                                    Long userId) {
        if (userVersion.entityVersion().equals(currentVersion.entityVersion())) {
            return null; // Pas de conflit
        }
        
        ConflictDTO conflict = new ConflictDTO();
        conflict.setConflictId(UUID.randomUUID().toString());
        conflict.setResourceType(ResourceType.MODULE_VERSION);
        conflict.setResourceId(currentVersion.id());
        conflict.setType(ConflictType.VERSION_CONFLICT);
        conflict.setDescription("Conflit détecté sur la version du module");
        
        // Informations sur les versions
        conflict.setOriginalVersion(userVersion.entityVersion());
        conflict.setCurrentVersion(currentVersion.entityVersion());
        conflict.setUserVersion(userVersion.entityVersion());
        
        // Informations utilisateur
        conflict.setCurrentUserId(userId);
        setUserInfo(conflict, userId, currentVersion.creator().id());
        conflict.setConflictCreatedAt(LocalDateTime.now());
        
        // Détection des conflits par champ
        List<FieldConflictDTO> fieldConflicts = detectFieldConflicts(userVersion, currentVersion);
        conflict.setFieldConflicts(fieldConflicts);
        
        // Détection des conflits de blocs
        List<FieldConflictDTO> blockConflicts = detectBlockConflicts(userVersion.blocks(), currentVersion.blocks());
        fieldConflicts.addAll(blockConflicts);
        
        conflict.setStatus(ConflictDTO.ConflictStatus.DETECTED);
        
        log.info("Conflit détecté pour ModuleVersion {}: {} conflits de champs", 
                currentVersion.id(), fieldConflicts.size());
        
        return conflict;
    }
    
    /**
     * Détecte les conflits entre deux blocs
     */
    public ConflictDTO detectBlockConflicts(BlockDTO userBlock, BlockDTO currentBlock, Long userId) {
        if (Objects.equals(userBlock.getEntityVersion(), currentBlock.getEntityVersion())) {
            return null; // Pas de conflit
        }
        
        ConflictDTO conflict = new ConflictDTO();
        conflict.setConflictId(UUID.randomUUID().toString());
        conflict.setResourceType(ResourceType.BLOCK);
        conflict.setResourceId(currentBlock.getId());
        conflict.setType(ConflictType.FIELD_CONFLICT);
        conflict.setDescription("Conflit détecté sur le bloc");
        
        // Informations sur les versions
        conflict.setOriginalVersion(userBlock.getEntityVersion());
        conflict.setCurrentVersion(currentBlock.getEntityVersion());
        conflict.setUserVersion(userBlock.getEntityVersion());
        
        // Informations utilisateur
        conflict.setCurrentUserId(userId);
        setUserInfo(conflict, userId, currentBlock.getCreator().id());
        conflict.setConflictCreatedAt(LocalDateTime.now());
        
        // Détection des conflits par champ
        List<FieldConflictDTO> fieldConflicts = detectBlockFieldConflicts(userBlock, currentBlock);
        conflict.setFieldConflicts(fieldConflicts);
        
        conflict.setStatus(ConflictDTO.ConflictStatus.DETECTED);
        
        return conflict;
    }
    
    /**
     * Détecte les conflits entre les champs de deux DTOs génériques
     */
    private List<FieldConflictDTO> detectFieldConflicts(Object userObject, Object currentObject) {
        List<FieldConflictDTO> conflicts = new ArrayList<>();
        
        if (userObject == null || currentObject == null) {
            return conflicts;
        }
        
        Class<?> clazz = userObject.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            // Ignorer certains champs
            if (shouldIgnoreField(field.getName())) {
                continue;
            }
            
            try {
                field.setAccessible(true);
                Object userValue = field.get(userObject);
                Object currentValue = field.get(currentObject);
                
                if (!Objects.equals(userValue, currentValue)) {
                    FieldConflictDTO fieldConflict = createFieldConflict(field.getName(), 
                            null, currentValue, userValue);
                    conflicts.add(fieldConflict);
                }
            } catch (IllegalAccessException e) {
                log.warn("Impossible d'accéder au champ {} pour la détection de conflit", field.getName());
            }
        }
        
        return conflicts;
    }
    
    /**
     * Détecte les conflits spécifiques aux blocs
     */
    private List<FieldConflictDTO> detectBlockFieldConflicts(BlockDTO userBlock, BlockDTO currentBlock) {
        List<FieldConflictDTO> conflicts = new ArrayList<>();
        
        // Conflits de base
        conflicts.addAll(detectFieldConflicts(userBlock, currentBlock));
        
        // Conflits spécifiques selon le type de bloc
        String blockType = userBlock.getType();
        switch (blockType) {
            case "paragraph":
                conflicts.addAll(detectParagraphBlockConflicts(userBlock, currentBlock));
                break;
            case "stat":
                conflicts.addAll(detectStatBlockConflicts(userBlock, currentBlock));
                break;
            case "music":
                conflicts.addAll(detectMusicBlockConflicts(userBlock, currentBlock));
                break;
            // Ajouter d'autres types selon vos besoins
        }
        
        return conflicts;
    }
    
    /**
     * Détecte les conflits dans les listes de blocs (ordre, suppressions, ajouts)
     */
    private List<FieldConflictDTO> detectBlockConflicts(List<BlockDTO> userBlocks, List<BlockDTO> currentBlocks) {
        List<FieldConflictDTO> conflicts = new ArrayList<>();
        
        if (userBlocks == null) userBlocks = new ArrayList<>();
        if (currentBlocks == null) currentBlocks = new ArrayList<>();
        
        // Créer des maps pour faciliter la comparaison
        Map<Long, BlockDTO> userBlockMap = new HashMap<>();
        Map<Long, BlockDTO> currentBlockMap = new HashMap<>();
        
        userBlocks.stream()
                .filter(b -> b.getId() != null)
                .forEach(b -> userBlockMap.put(b.getId(), b));
        
        currentBlocks.stream()
                .filter(b -> b.getId() != null)
                .forEach(b -> currentBlockMap.put(b.getId(), b));
        
        // Détecter les conflits d'ordre
        if (userBlocks.size() != currentBlocks.size() || hasOrderChanged(userBlocks, currentBlocks)) {
            FieldConflictDTO orderConflict = new FieldConflictDTO();
            orderConflict.setFieldName("blockOrder");
            orderConflict.setFieldDisplayName("Ordre des blocs");
            orderConflict.setType(ConflictType.BLOCK_ORDER_CONFLICT);
            orderConflict.setCurrentValue(extractBlockOrder(currentBlocks));
            orderConflict.setUserValue(extractBlockOrder(userBlocks));
            orderConflict.setCanAutoResolve(false);
            orderConflict.setDescription("L'ordre des blocs a été modifié");
            conflicts.add(orderConflict);
        }
        
        return conflicts;
    }
    
    /**
     * Méthodes spécialisées pour les différents types de blocs
     */
    private List<FieldConflictDTO> detectParagraphBlockConflicts(BlockDTO userBlock, BlockDTO currentBlock) {
        // Implémentation spécifique pour les blocs de paragraphe
        return new ArrayList<>();
    }
    
    private List<FieldConflictDTO> detectStatBlockConflicts(BlockDTO userBlock, BlockDTO currentBlock) {
        // Implémentation spécifique pour les blocs de statistiques
        return new ArrayList<>();
    }
    
    private List<FieldConflictDTO> detectMusicBlockConflicts(BlockDTO userBlock, BlockDTO currentBlock) {
        // Implémentation spécifique pour les blocs musicaux
        return new ArrayList<>();
    }
    
    /**
     * Méthodes utilitaires
     */
    private FieldConflictDTO createFieldConflict(String fieldName, Object originalValue, 
                                                Object currentValue, Object userValue) {
        FieldConflictDTO conflict = new FieldConflictDTO();
        conflict.setFieldName(fieldName);
        conflict.setFieldDisplayName(getDisplayName(fieldName));
        conflict.setOriginalValue(originalValue);
        conflict.setCurrentValue(currentValue);
        conflict.setUserValue(userValue);
        conflict.setType(ConflictType.FIELD_CONFLICT);
        conflict.setFieldType(getFieldType(userValue));
        conflict.setCanAutoResolve(canAutoResolve(fieldName, currentValue, userValue));
        conflict.setDescription(generateConflictDescription(fieldName, currentValue, userValue));
        
        return conflict;
    }
    
    private void setUserInfo(ConflictDTO conflict, Long currentUserId, Long conflictingUserId) {
        userRepository.findById(currentUserId).ifPresent(user -> 
                conflict.setCurrentUsername(user.getUsername()));
        userRepository.findById(conflictingUserId).ifPresent(user -> {
            conflict.setConflictingUserId(conflictingUserId);
            conflict.setConflictingUsername(user.getUsername());
        });
    }
    
    private boolean shouldIgnoreField(String fieldName) {
        return fieldName.equals("id") || 
               fieldName.equals("createdAt") || 
               fieldName.equals("updatedAt") ||
               fieldName.equals("entityVersion") ||
               fieldName.equals("lastModified");
    }
    
    private String getDisplayName(String fieldName) {
        Map<String, String> displayNames = Map.of(
            "title", "Titre",
            "description", "Description",
            "paragraph", "Contenu",
            "blockOrder", "Ordre du bloc",
            "published", "Statut de publication"
        );
        return displayNames.getOrDefault(fieldName, fieldName);
    }
    
    private String getFieldType(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "text";
        if (value instanceof Number) return "number";
        if (value instanceof Boolean) return "boolean";
        return "object";
    }
    
    private boolean canAutoResolve(String fieldName, Object currentValue, Object userValue) {
        // Logique simple : les champs numériques et booléens peuvent être auto-résolus
        return userValue instanceof Number || userValue instanceof Boolean;
    }
    
    private String generateConflictDescription(String fieldName, Object currentValue, Object userValue) {
        return String.format("Le champ '%s' a été modifié. Valeur actuelle: '%s', votre valeur: '%s'", 
                getDisplayName(fieldName), currentValue, userValue);
    }
    
    private boolean hasOrderChanged(List<BlockDTO> userBlocks, List<BlockDTO> currentBlocks) {
        if (userBlocks.size() != currentBlocks.size()) return true;
        
        for (int i = 0; i < userBlocks.size(); i++) {
            BlockDTO userBlock = userBlocks.get(i);
            BlockDTO currentBlock = currentBlocks.get(i);
            
            if (userBlock.getId() != null && currentBlock.getId() != null && 
                !userBlock.getId().equals(currentBlock.getId())) {
                return true;
            }
        }
        return false;
    }
    
    private List<Long> extractBlockOrder(List<BlockDTO> blocks) {
        return blocks.stream()
                .map(BlockDTO::getId)
                .filter(Objects::nonNull)
                .toList();
    }
}