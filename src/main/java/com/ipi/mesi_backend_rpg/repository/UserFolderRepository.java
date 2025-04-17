package com.ipi.mesi_backend_rpg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ipi.mesi_backend_rpg.model.UserFolder;

@Repository
public interface UserFolderRepository extends JpaRepository<UserFolder, Long> {

    /**
     * Recherche tous les dossiers appartenant à un utilisateur spécifique.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return Une liste de dossiers appartenant à l'utilisateur.
     */
    List<UserFolder> findByUserId(Long userId);

    /**
     * Recherche tous les dossiers enfants d'un dossier parent.
     *
     * @param parentFolderId L'identifiant du dossier parent.
     * @return Une liste des dossiers enfants.
     */
    List<UserFolder> findByParentFolder(Long parentFolderId);

    /**
     * Recherche les dossiers racines d'un utilisateur (parent_folder est null).
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return Une liste des dossiers racines de l'utilisateur.
     */
    List<UserFolder> findByUserIdAndParentFolderIsNull(Long userId);

    /**
     * Recherche les dossiers d'un utilisateur dont le nom contient une chaîne
     * spécifique.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @param name   La chaîne à rechercher dans le nom des dossiers.
     * @return Une liste des dossiers correspondant aux critères.
     */
    List<UserFolder> findByUserIdAndNameContaining(Long userId, String name);
}
