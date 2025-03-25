package com.ipi.mesi_backend_rpg;

import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class MesiBackendRpgApplication {

    public static void main(String[] args) {
        SpringApplication.run(MesiBackendRpgApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(UserRepository userRepository) {
        return (args) -> {
            System.out.println("Début de l'exécution du CommandLineRunner...");

            // Créez un utilisateur avec un mot de passe haché
            String rawPassword = "password123";

            User user = new User();
            user.setUsername("john_doe");
            user.setPassword("password123");  // Mot de passe haché
            user.setEmail("john.doe@example.com");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            // Sauvegarder l'utilisateur dans la base de données
            userRepository.save(user);

            // Log pour vérifier que l'utilisateur a été sauvegardé
            System.out.println("Utilisateur créé avec succès : " + user.getUsername());

            // Vérifiez si l'utilisateur est bien sauvé dans la base de données
            System.out.println("Utilisateur récupéré de la base de données : " + userRepository.findById(user.getId()).orElse(null));
        };
    }

}
