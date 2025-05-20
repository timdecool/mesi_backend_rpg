package com.ipi.mesi_backend_rpg.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.ModuleUpdateDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleAccess;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.ModuleAccessRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CollaborativeEditingService {

    private final ModuleRepository moduleRepository;
    private final ModuleAccessRepository moduleAccessRepository;
    private final UserRepository userRepository;

    // Map pour suivre les utilisateurs actifs sur chaque module
    private final Map<Long, Map<Long, String>> activeUsers = new HashMap<>();

    public boolean canAccessModule(Long moduleId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return false;

        Module module = moduleRepository.findById(moduleId).orElse(null);
        if (module == null)
            return false;

        // Vérifier si l'utilisateur a accès au module
        List<ModuleAccess> accesses = moduleAccessRepository.findAllByUser(user);
        return accesses.stream()
                .anyMatch(access -> access.getModule().getId() == moduleId && access.isCanEdit());
    }

    public void processUpdate(Long moduleId, ModuleUpdateDTO update) {
        if (!canAccessModule(moduleId, update.getUserId())) {
            return; // Rejeter la mise à jour si l'utilisateur n'a pas les droits
        }

        // Logique pour appliquer la mise à jour au module
        // Cela pourrait impliquer la mise à jour des blocs de contenu en base de
        // données
        // ou la mise en cache des changements pour les appliquer de manière groupée

        // Suivre l'activité de l'utilisateur
        trackUserActivity(moduleId, update.getUserId(), update.getUsername());
    }

    public void trackUserActivity(Long moduleId, Long userId, String username) {
        // Ajouter l'utilisateur à la liste des utilisateurs actifs pour ce module
        activeUsers.computeIfAbsent(moduleId, k -> new HashMap<>()).put(userId, username);
    }

    public Map<Long, String> getActiveUsers(Long moduleId) {
        return activeUsers.getOrDefault(moduleId, new HashMap<>());
    }
}