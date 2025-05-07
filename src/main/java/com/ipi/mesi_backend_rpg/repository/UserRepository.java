package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son adresse email.
     *
     * @param email L'adresse email à rechercher.
     * @return Un Optional contenant l'utilisateur s'il est trouvé, sinon Optional.empty().
     */
    Optional<User> findByEmail(String email); 
    /**
     * Recherche les 20 premiers utilisateurs dont le nom d'utilisateur contient la chaîne de caractères fournie (insensible à la casse).
     *
     * @param username La sous-chaîne à rechercher dans les noms d'utilisateur.
     * @return Une liste des 20 premiers utilisateurs correspondants au plus.
     */
    List<User> findFirst20ByUsernameContainingIgnoreCase(String username);

    /**
     * Recherche les 20 premiers utilisateurs dont l'email contient la chaîne de caractères fournie (insensible à la casse).
     *
     * @param email La sous-chaîne à rechercher dans les emails.
     * @return Une liste des 20 premiers utilisateurs correspondants au plus.
     */
    List<User> findFirst20ByEmailContainingIgnoreCase(String email);
}
