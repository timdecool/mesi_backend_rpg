package com.ipi.mesi_backend_rpg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ipi.mesi_backend_rpg.model.UserSavedModule;

@Repository
public interface UserSavedModuleRepository extends JpaRepository<UserSavedModule, Long> {

    /**
     * Recherche tous les modules sauvegardés par un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @return Une liste des modules sauvegardés par l'utilisateur.
     */
    List<UserSavedModule> findByUserId(Long userId);

    /**
     * Recherche tous les modules sauvegardés dans un dossier spécifique.
     *
     * @param folderId L'identifiant du dossier.
     * @return Une liste des modules sauvegardés dans le dossier.
     */
    List<UserSavedModule> findByFolderId(Long folderId);

    /**
     * Recherche tous les modules sauvegardés par un utilisateur dans un dossier
     * spécifique.
     *
     * @param userId   L'identifiant de l'utilisateur.
     * @param folderId L'identifiant du dossier.
     * @return Une liste des modules sauvegardés correspondant aux critères.
     */
    List<UserSavedModule> findByUserIdAndFolderId(Long userId, Long folderId);

    /**
     * Recherche tous les modules sauvegardés d'un module spécifique par un
     * utilisateur.
     *
     * @param userId   L'identifiant de l'utilisateur.
     * @param moduleId L'identifiant du module.
     * @return Une liste des modules sauvegardés correspondant aux critères.
     */
    List<UserSavedModule> findByUserIdAndModuleId(Long userId, Long moduleId);

    /**
     * Recherche les modules sauvegardés par leur alias.
     *
     * @param userId L'identifiant de l'utilisateur.
     * @param alias  L'alias ou partie de l'alias à rechercher.
     * @return Une liste des modules sauvegardés correspondant aux critères.
     */
    List<UserSavedModule> findByUserIdAndAliasContaining(Long userId, String alias);
}
