package com.ipi.mesi_backend_rpg;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.google.firebase.ErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

@SpringBootApplication
public class MesiBackendRpgApplication {
    private static final Logger logger = LoggerFactory.getLogger(MesiBackendRpgApplication.class);

    @Value("${firebase.web.api.key}")
    private String firebaseApiKey;

    @Value("${email}")
    private String email;

    @Value("${mdp}")
    private String mdp;

    public static void main(String[] args) {
        SpringApplication.run(MesiBackendRpgApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(UserRepository userRepository,
            GameSystemRepository gameSystemRepository,
            FirebaseAuth firebaseAuth,
            WebClient.Builder webClientBuilder) {
        return (args) -> {
            logger.info("Debut de l'execution du CommandLineRunner...");

            try {
                User localUser = new User();
                localUser.setUsername("john_doe");
                localUser.setEmail("john.doe@example.com");

                localUser.setCreatedAt(LocalDateTime.now());
                localUser.setUpdatedAt(LocalDateTime.now());
                if (userRepository.findByEmail(localUser.getEmail()).isEmpty()) {
                    userRepository.save(localUser);
                    logger.info("Utilisateur local cree avec succes : {}", localUser.getEmail());
                } else {
                    logger.info("Utilisateur local {} existe déjà.", localUser.getEmail());
                }

                GameSystem gameSystem = new GameSystem();
                gameSystem.setName("Donjon et Dragon");
                gameSystem.setCreatedAt(LocalDate.now());
                gameSystem.setUpdatedAt(LocalDate.now());
                if (gameSystemRepository.findById(1L).isEmpty()) { // Exemple: on suppose ID 1 pour D&D
                    gameSystemRepository.save(gameSystem);
                    logger.info("GameSystem local cree : {}", gameSystem.getName());
                } else {
                    logger.info("GameSystem {} existe déjà.", gameSystem.getName());
                }

            } catch (Exception e) {
                logger.error("Erreur lors de l'initialisation des données locales : {}", e.getMessage(), e);
            }
            logger.info("Initialisation données locales terminee.");

            logger.info("Verification/Creation de l'utilisateur de test Firebase...");
            String testEmail = this.email; 
            String testPassword = this.mdp;
            String uid = null;

            try {
                UserRecord userRecord = firebaseAuth.getUserByEmail(testEmail);
                uid = userRecord.getUid();
                logger.info("Utilisateur Firebase trouve : {} (UID: {})", userRecord.getEmail(), uid);
            } catch (FirebaseAuthException e) {
                if (e.getErrorCode() != null && ErrorCode.NOT_FOUND.equals(e.getErrorCode())) {
                    logger.warn("Utilisateur Firebase '{}' non trouve. Tentative de création...", testEmail);
                    UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                            .setEmail(testEmail)
                            .setEmailVerified(true)
                            .setPassword(testPassword)
                            .setDisplayName("Test Dev User")
                            .setDisabled(false);
                    try {
                        UserRecord createdUser = firebaseAuth.createUser(request);
                        uid = createdUser.getUid();
                        logger.info("Utilisateur Firebase cree avec succes : {} (UID: {})", createdUser.getEmail(),
                                uid);
                    } catch (FirebaseAuthException createUserEx) {
                        logger.error("Erreur Firebase lors de la CREATION de l'utilisateur '{}' : {}", testEmail,
                                createUserEx.getMessage(), createUserEx);
                    }
                } else {
                    logger.error("Erreur Firebase lors de la RECUPERATION de l'utilisateur '{}' : {}", testEmail,
                            e.getMessage(), e);
                }
            }

            if (uid != null) { // On ne tente la connexion que si l'utilisateur existe
                logger.info("Tentative de connexion via l'API REST Firebase pour obtenir l'ID Token...");
                WebClient webClient = webClientBuilder.baseUrl("https://identitytoolkit.googleapis.com/v1").build();

                Map<String, Object> requestBody = Map.of(
                        "email", testEmail,
                        "password", testPassword,
                        "returnSecureToken", true);

                try {
                    ParameterizedTypeReference<Map<String, Object>> mapTypeReference = new ParameterizedTypeReference<Map<String, Object>>() {
                    };

                    Map<String, Object> response = webClient.post()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/accounts:signInWithPassword")
                                    .queryParam("key", firebaseApiKey)
                                    .build())
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(mapTypeReference)
                            .block();

                    // Traiter la réponse
                    if (response != null && response.containsKey("idToken")) {
                        String idToken = (String) response.get("idToken"); // Récupérer l'ID Token
                        logger.info("------------------------------------------------------------------");
                        logger.info("--- ID TOKEN FIREBASE pour les tests (UID: {}) ---", uid);
                        logger.info(">>> Copiez cet ID TOKEN ci-dessous pour Postman : <<<");
                        System.out.println("\nFIREBASE_ID_TOKEN=" + idToken + "\n");
                        logger.info("Ce token peut etre utilise directement avec 'Bearer' dans Postman.");
                        logger.info("------------------------------------------------------------------");
                    } else {
                        // Si la réponse ne contient pas l'idToken attendu
                        logger.error("La réponse de l'API REST Firebase ne contient pas d'idToken. Reponse reçue : {}",
                                response);
                    }

                } catch (WebClientResponseException e) {
                    logger.error(
                            "Erreur lors de l'appel à l'API REST Firebase signInWithPassword : Statut={}, Réponse={}",
                            e.getStatusCode(), e.getResponseBodyAsString(), e);
                } catch (Exception e) {
                    logger.error("Erreur inattendue lors de la connexion via l'API REST Firebase : {}", e.getMessage(),
                            e);
                }

            } else {
                // Si l'UID n'a pas pu être déterminé à l'étape 2
                logger.error(
                        "Impossible de tenter la connexion REST car l'UID de l'utilisateur '{}' n'a pas pu être déterminé.",
                        testEmail);
            }

            logger.info("Fin de l'execution du CommandLineRunner.");
        };
    }
}