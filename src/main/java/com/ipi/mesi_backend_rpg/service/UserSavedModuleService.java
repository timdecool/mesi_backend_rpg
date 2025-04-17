package com.ipi.mesi_backend_rpg.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.UserSavedModuleDTO;
import com.ipi.mesi_backend_rpg.mapper.UserSavedModuleMapper;
import com.ipi.mesi_backend_rpg.model.UserSavedModule;
import com.ipi.mesi_backend_rpg.repository.UserSavedModuleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Service pour gérer les opérations liées aux modules sauvegardés par les
 * utilisateurs.
 */
@Service
@RequiredArgsConstructor
public class UserSavedModuleService {
    private final UserSavedModuleRepository userSavedModuleRepository;
    private final UserSavedModuleMapper userSavedModuleMapper;

    /**
     * Récupère tous les modules sauvegardés par un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return Une liste de DTOs des modules sauvegardés.
     */
    public List<UserSavedModuleDTO> getAllModulesByUserId(Long userId) {
        return userSavedModuleRepository.findByUserId(userId)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un module sauvegardé par son identifiant.
     *
     * @param savedModuleId L'identifiant du module sauvegardé.
     * @return Un Optional contenant le DTO du module s'il est trouvé, sinon
     *         Optional.empty().
     */
    public Optional<UserSavedModuleDTO> getSavedModuleById(Long savedModuleId) {
        return userSavedModuleRepository.findById(savedModuleId)
                .map(userSavedModuleMapper::toDTO);
    }

    /**
     * Récupère tous les modules sauvegardés dans un dossier.
     *
     * @param folderId L'identifiant du dossier.
     * @return Une liste de DTOs des modules sauvegardés dans le dossier.
     */
    public List<UserSavedModuleDTO> getModulesByFolderId(Long folderId) {
        return userSavedModuleRepository.findByFolderId(folderId)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les modules sauvegardés par un utilisateur dans un dossier.
     *
     * @param userId   L'identifiant de l'utilisateur.
     * @param folderId L'identifiant du dossier.
     * @return Une liste de DTOs des modules sauvegardés correspondant aux critères.
     */
    public List<UserSavedModuleDTO> getModulesByUserIdAndFolderId(Long userId, Long folderId) {
        return userSavedModuleRepository.findByUserIdAndFolderId(userId, folderId)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Recherche les modules sauvegardés par alias.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @param alias  L'alias ou partie de l'alias à rechercher.
     * @return Une liste de DTOs des modules sauvegardés correspondant aux critères.
     */
    public List<UserSavedModuleDTO> searchModulesByAlias(Long userId, String alias) {
        return userSavedModuleRepository.findByUserIdAndAliasContaining(userId, alias)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crée un nouveau module sauvegardé.
     *
     * @param userSavedModuleDTO Les données du module à sauvegarder.
     * @return Le DTO du module sauvegardé créé.
     */
    @Transactional
    public UserSavedModuleDTO createSavedModule(UserSavedModuleDTO userSavedModuleDTO) {
        UserSavedModule userSavedModule = userSavedModuleMapper.toEntity(userSavedModuleDTO);
        userSavedModule.setSaved_module_id(null); // Assure que l'ID sera généré

        UserSavedModule savedModule = userSavedModuleRepository.save(userSavedModule);
        return userSavedModuleMapper.toDTO(savedModule);
    }

    /**
     * Met à jour un module sauvegardé existant.
     *
     * @param savedModuleId      L'identifiant du module sauvegardé à mettre à jour.
     * @param userSavedModuleDTO Les nouvelles données du module sauvegardé.
     * @return Un Optional contenant le DTO du module mis à jour s'il est trouvé,
     *         sinon Optional.empty().
     */
    @Transactional
    public Optional<UserSavedModuleDTO> updateSavedModule(Long savedModuleId, UserSavedModuleDTO userSavedModuleDTO) {
        return userSavedModuleRepository.findById(savedModuleId)
                .map(module -> {
                    module.setModule_id(userSavedModuleDTO.module_id());
                    module.setModule_version_id(userSavedModuleDTO.module_version_id());
                    module.setFolder_id(userSavedModuleDTO.folder_id());
                    module.setAlias(userSavedModuleDTO.alias());
                    return userSavedModuleMapper.toDTO(userSavedModuleRepository.save(module));
                });
    }

    /**
     * Déplace des modules sauvegardés vers un autre dossier.
     *
     * @param savedModuleIds Liste des identifiants des modules à déplacer.
     * @param targetFolderId L'identifiant du dossier cible.
     * @return true si tous les modules ont été déplacés avec succès, false sinon.
     */
    @Transactional
    public boolean moveModulesToFolder(List<Long> savedModuleIds, Long targetFolderId) {
        List<UserSavedModule> modules = userSavedModuleRepository.findAllById(savedModuleIds);

        if (modules.size() != savedModuleIds.size()) {
            return false; // Certains modules n'ont pas été trouvés
        }

        modules.forEach(module -> module.setFolder_id(targetFolderId));
        userSavedModuleRepository.saveAll(modules);

        return true;
    }

    /**
     * Supprime un module sauvegardé par son identifiant.
     *
     * @param savedModuleId L'identifiant du module sauvegardé à supprimer.
     * @return true si le module a été supprimé, false s'il n'a pas été trouvé.
     */
    @Transactional
    public boolean deleteSavedModule(Long savedModuleId) {
        if (userSavedModuleRepository.existsById(savedModuleId)) {
            userSavedModuleRepository.deleteById(savedModuleId);
            return true;
        }
        return false;
    }

    /**
     * Supprime tous les modules sauvegardés dans un dossier.
     *
     * @param folderId L'identifiant du dossier.
     * @return Le nombre de modules supprimés.
     */
    @Transactional
    public long deleteModulesByFolderId(Long folderId) {
        List<UserSavedModule> modules = userSavedModuleRepository.findByFolderId(folderId);
        userSavedModuleRepository.deleteAll(modules);
        return modules.size();
    }
}
