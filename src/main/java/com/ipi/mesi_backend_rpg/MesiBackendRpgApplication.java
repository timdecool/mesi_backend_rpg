package com.ipi.mesi_backend_rpg;

import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class MesiBackendRpgApplication {

    public static void main(String[] args) {
        SpringApplication.run(MesiBackendRpgApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(UserRepository userRepository, GameSystemRepository gameSystemRepository) {
        return (args) -> {
            System.out.println("Début de l'exécution du CommandLineRunner...");

            // Création User Test

            // String rawPassword = "password123";
            User user = new User();
            user.setUsername("john_doe");
            user.setEmail("john.doe@example.com");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            System.out.println("Utilisateur créé avec succès : " + user.getUsername());
            System.out.println("Utilisateur récupéré de la base de données : " + userRepository.findById(user.getId()).orElse(null));


            //Création d'un system de jeu par défaut
            GameSystem gameSystem = new GameSystem();
            gameSystem.setName("Donjon et Dragon");
            gameSystem.setCreatedAt(LocalDate.now());
            gameSystem.setUpdatedAt(LocalDate.now());
            gameSystemRepository.save(gameSystem);

        };
    }

}
