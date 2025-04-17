package com.ipi.mesi_backend_rpg.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.UserFolderDTO;
import com.ipi.mesi_backend_rpg.mapper.UserFolderMapper;
import com.ipi.mesi_backend_rpg.model.UserFolder;
import com.ipi.mesi_backend_rpg.repository.UserFolderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Service pour gérer les opérations liées aux dossiers utilisateurs.
 */
@Service
@RequiredArgsConstructor
public class UserFolderService {

    private final UserFolderRepository userFolderRepository;
    private final UserFolderMapper userFolderMapper;

    /**
     * Récupère tous les dossiers d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return Une liste de DTOs des dossiers de l'utilisateur.
     */
    public List<UserFolderDTO> getAllFoldersByUserId(Long userId) {
        return userFolderRepository.findByUserId(userId)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un dossier par son identifiant.
     *
     * @param folderId L'identifiant du dossier.
     * @return Un Optional contenant le DTO du dossier s'il est trouvé, sinon
     *         Optional.empty().
     */
    public Optional<UserFolderDTO> getFolderById(Long folderId) {
        return userFolderRepository.findById(folderId)
                .map(userFolderMapper::toDTO);
    }

    /**
     * Récupère tous les dossiers enfants d'un dossier parent.
     *
     * @param parentFolderId L'identifiant du dossier parent.
     * @return Une liste de DTOs des dossiers enfants.
     */
    public List<UserFolderDTO> getChildFolders(Long parentFolderId) {
        return userFolderRepository.findByParentFolder(parentFolderId)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les dossiers racines d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return Une liste de DTOs des dossiers racines.
     */
    public List<UserFolderDTO> getRootFolders(Long userId) {
        return userFolderRepository.findByUserIdAndParentFolderIsNull(userId)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Recherche les dossiers d'un utilisateur par nom.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @param name   Le nom ou partie du nom à rechercher.
     * @return Une liste de DTOs des dossiers correspondants.
     */
    public List<UserFolderDTO> searchFoldersByName(Long userId, String name) {
        return userFolderRepository.findByUserIdAndNameContaining(userId, name)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crée un nouveau dossier.
     *
     * @param userFolderDTO Les données du dossier à créer.
     * @return Le DTO du dossier créé.
     */
    @Transactional
    public UserFolderDTO createFolder(UserFolderDTO userFolderDTO) {
        UserFolder userFolder = new UserFolder(
                null, // L'ID sera généré automatiquement
                userFolderDTO.user_id(),
                userFolderDTO.name(),
                userFolderDTO.parent_folder());

        UserFolder savedFolder = userFolderRepository.save(userFolder);
        return userFolderMapper.toDTO(savedFolder);
    }

    /**
     * Met à jour un dossier existant.
     *
     * @param folderId      L'identifiant du dossier à mettre à jour.
     * @param userFolderDTO Les nouvelles données du dossier.
     * @return Un Optional contenant le DTO du dossier mis à jour s'il est trouvé,
     *         sinon Optional.empty().
     */
    @Transactional
    public Optional<UserFolderDTO> updateFolder(Long folderId, UserFolderDTO userFolderDTO) {
        return userFolderRepository.findById(folderId)
                .map(folder -> {
                    folder.setName(userFolderDTO.name());
                    folder.setParent_folder(userFolderDTO.parent_folder());
                    return userFolderMapper.toDTO(userFolderRepository.save(folder));
                });
    }

    /**
     * Supprime un dossier par son identifiant.
     *
     * @param folderId L'identifiant du dossier à supprimer.
     * @return true si le dossier a été supprimé, false s'il n'a pas été trouvé.
     */
    @Transactional
    public boolean deleteFolder(Long folderId) {
        if (userFolderRepository.existsById(folderId)) {
            userFolderRepository.deleteById(folderId);
            return true;
        }
        return false;
    }
}
