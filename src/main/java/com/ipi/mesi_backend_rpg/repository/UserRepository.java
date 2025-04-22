package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.User;

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
}
