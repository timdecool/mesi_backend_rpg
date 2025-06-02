package com.ipi.mesi_backend_rpg.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.model.FieldConflict;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ObjectComparisonService {
    
    /**
     * Compare deux objets et retourne la liste des différences
     */
    public List<FieldConflict> compareObjects(Object original, Object current, Object user) {
        List<FieldConflict> conflicts = new ArrayList<>();
        
        if (original == null || current == null || user == null) {
            log.warn("Un des objets à comparer est null");
            return conflicts;
        }
        
        Class<?> clazz = original.getClass();
        if (!clazz.equals(current.getClass()) || !clazz.equals(user.getClass())) {
            log.warn("Les objets à comparer ne sont pas du même type");
            return conflicts;
        }
        
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            if (shouldIgnoreField(field)) {
                continue;
            }
            
            try {
                field.setAccessible(true);
                Object originalValue = field.get(original);
                Object currentValue = field.get(current);
                Object userValue = field.get(user);
                
                FieldConflict conflict = analyzeFieldConflict(field.getName(), originalValue, currentValue, userValue);
                if (conflict != null) {
                    conflicts.add(conflict);
                }
                
            } catch (IllegalAccessException e) {
                log.warn("Impossible d'accéder au champ {} pour la comparaison", field.getName());
            }
        }
        
        return conflicts;
    }
    
    /**
     * Analyse un conflit spécifique sur un champ
     */
    private FieldConflict analyzeFieldConflict(String fieldName, Object originalValue, 
                                             Object currentValue, Object userValue) {
        
        // Pas de conflit si les valeurs sont identiques
        if (Objects.equals(currentValue, userValue)) {
            return null;
        }
        
        // Déterminer le type de conflit
        boolean originalEqualsUser = Objects.equals(originalValue, userValue);
        boolean originalEqualsCurrent = Objects.equals(originalValue, currentValue);
        
        if (originalEqualsUser && !originalEqualsCurrent) {
            // L'utilisateur n'a pas modifié, mais la version courante a été modifiée
            log.debug("Champ {} modifié par un autre utilisateur", fieldName);
            return null; // Pas de conflit réel, on peut prendre la version courante
        }
        
        if (!originalEqualsUser && originalEqualsCurrent) {
            // L'utilisateur a modifié, mais pas la version courante
            log.debug("Champ {} modifié uniquement par l'utilisateur", fieldName);
            return null; // Pas de conflit, on peut prendre la version utilisateur
        }
        
        if (!originalEqualsUser && !originalEqualsCurrent) {
            // Les deux ont modifié le champ
            log.debug("Conflit détecté sur le champ {}", fieldName);
            return createFieldConflict(fieldName, originalValue, currentValue, userValue);
        }
        
        return null;
    }
    
    /**
     * Crée un objet FieldConflict
     */
    private FieldConflict createFieldConflict(String fieldName, Object originalValue, 
                                            Object currentValue, Object userValue) {
        FieldConflict conflict = new FieldConflict();
        conflict.setFieldName(fieldName);
        conflict.setOriginalValue(originalValue);
        conflict.setCurrentValue(currentValue);
        conflict.setUserValue(userValue);
        conflict.setDescription(generateConflictDescription(fieldName, currentValue, userValue));
        
        return conflict;
    }
    
    /**
     * Compare deux listes et détecte les différences
     */
    public <T> ListComparisonResult<T> compareLists(List<T> originalList, List<T> currentList, List<T> userList) {
        ListComparisonResult<T> result = new ListComparisonResult<>();
        
        // Simplification: comparer par taille et ordre
        result.setSizeChanged(originalList.size() != currentList.size() || originalList.size() != userList.size());
        result.setOrderChanged(hasOrderChanged(originalList, currentList) || hasOrderChanged(originalList, userList));
        
        // Détecter les éléments ajoutés/supprimés/modifiés
        result.setAddedInCurrent(findAddedElements(originalList, currentList));
        result.setRemovedInCurrent(findRemovedElements(originalList, currentList));
        result.setAddedInUser(findAddedElements(originalList, userList));
        result.setRemovedInUser(findRemovedElements(originalList, userList));
        
        return result;
    }
    
    /**
     * Vérifie si l'ordre a changé dans une liste
     */
    private <T> boolean hasOrderChanged(List<T> original, List<T> modified) {
        if (original.size() != modified.size()) {
            return true;
        }
        
        for (int i = 0; i < original.size(); i++) {
            if (!Objects.equals(original.get(i), modified.get(i))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Trouve les éléments ajoutés
     */
    private <T> List<T> findAddedElements(List<T> original, List<T> modified) {
        List<T> added = new ArrayList<>();
        
        for (T element : modified) {
            if (!original.contains(element)) {
                added.add(element);
            }
        }
        
        return added;
    }
    
    /**
     * Trouve les éléments supprimés
     */
    private <T> List<T> findRemovedElements(List<T> original, List<T> modified) {
        List<T> removed = new ArrayList<>();
        
        for (T element : original) {
            if (!modified.contains(element)) {
                removed.add(element);
            }
        }
        
        return removed;
    }
    
    /**
     * Génère une description de conflit
     */
    private String generateConflictDescription(String fieldName, Object currentValue, Object userValue) {
        return String.format("Conflit sur le champ '%s': valeur actuelle='%s', votre valeur='%s'", 
                fieldName, currentValue, userValue);
    }
    
    /**
     * Détermine si un champ doit être ignoré lors de la comparaison
     */
    private boolean shouldIgnoreField(Field field) {
        String fieldName = field.getName();
        
        // Ignorer les champs techniques et de métadonnées
        return fieldName.equals("id") ||
               fieldName.equals("createdAt") ||
               fieldName.equals("updatedAt") ||
               fieldName.equals("lastModified") ||
               fieldName.equals("version") ||
               fieldName.equals("entityVersion") ||
               fieldName.startsWith("$") ||
               java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
               java.lang.reflect.Modifier.isTransient(field.getModifiers());
    }
    
    /**
     * Calcule un score de similarité entre deux objets (0.0 = complètement différents, 1.0 = identiques)
     */
    public double calculateSimilarity(Object obj1, Object obj2) {
        if (Objects.equals(obj1, obj2)) {
            return 1.0;
        }
        
        if (obj1 == null || obj2 == null) {
            return 0.0;
        }
        
        Class<?> clazz = obj1.getClass();
        if (!clazz.equals(obj2.getClass())) {
            return 0.0;
        }
        
        Field[] fields = clazz.getDeclaredFields();
        int totalFields = 0;
        int matchingFields = 0;
        
        for (Field field : fields) {
            if (shouldIgnoreField(field)) {
                continue;
            }
            
            try {
                field.setAccessible(true);
                Object value1 = field.get(obj1);
                Object value2 = field.get(obj2);
                
                totalFields++;
                if (Objects.equals(value1, value2)) {
                    matchingFields++;
                }
                
            } catch (IllegalAccessException e) {
                log.warn("Impossible d'accéder au champ {} pour le calcul de similarité", field.getName());
            }
        }
        
        return totalFields == 0 ? 1.0 : (double) matchingFields / totalFields;
    }
    
    /**
     * Classe de résultat pour la comparaison de listes
     */
    public static class ListComparisonResult<T> {
        private boolean sizeChanged;
        private boolean orderChanged;
        private List<T> addedInCurrent = new ArrayList<>();
        private List<T> removedInCurrent = new ArrayList<>();
        private List<T> addedInUser = new ArrayList<>();
        private List<T> removedInUser = new ArrayList<>();
        
        // Getters et Setters
        public boolean isSizeChanged() { return sizeChanged; }
        public void setSizeChanged(boolean sizeChanged) { this.sizeChanged = sizeChanged; }
        
        public boolean isOrderChanged() { return orderChanged; }
        public void setOrderChanged(boolean orderChanged) { this.orderChanged = orderChanged; }
        
        public List<T> getAddedInCurrent() { return addedInCurrent; }
        public void setAddedInCurrent(List<T> addedInCurrent) { this.addedInCurrent = addedInCurrent; }
        
        public List<T> getRemovedInCurrent() { return removedInCurrent; }
        public void setRemovedInCurrent(List<T> removedInCurrent) { this.removedInCurrent = removedInCurrent; }
        
        public List<T> getAddedInUser() { return addedInUser; }
        public void setAddedInUser(List<T> addedInUser) { this.addedInUser = addedInUser; }
        
        public List<T> getRemovedInUser() { return removedInUser; }
        public void setRemovedInUser(List<T> removedInUser) { this.removedInUser = removedInUser; }
        
        public boolean hasConflicts() {
            return sizeChanged || orderChanged || !addedInCurrent.isEmpty() || 
                   !removedInCurrent.isEmpty() || !addedInUser.isEmpty() || !removedInUser.isEmpty();
        }
    }
}