package com.ipi.mesi_backend_rpg.configuration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

@Configuration
public class FireBaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = null;

            // Tenter plusieurs emplacements pour trouver le fichier Firebase
            try {
                // 1. Essayer d'abord le classpath (méthode originale)
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                serviceAccount = resource.getInputStream();
            } catch (IOException e) {
                // 2. Essayer la propriété système
                String firebasePath = System.getProperty("firebase.service.account.path");
                if (firebasePath != null) {
                    File file = new File(firebasePath);
                    if (file.exists()) {
                        serviceAccount = new FileInputStream(file);
                    }
                }

                // 3. Essayer des emplacements fixes
                if (serviceAccount == null) {
                    String[] paths = {
                            "/app/firebase-service-account.json",
                            "/app/src/main/resources/firebase-service-account.json",
                            "/firebase-service-account.json"
                    };

                    for (String path : paths) {
                        File file = new File(path);
                        if (file.exists()) {
                            serviceAccount = new FileInputStream(file);
                            break;
                        }
                    }
                }

                // 4. En dernier recours, utiliser un fichier vide
                if (serviceAccount == null) {
                    serviceAccount = new ByteArrayInputStream("{}".getBytes());
                }
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId("jdr-mesi")
                    .setStorageBucket("jdr-mesi.firebasestorage.app")
                    .build();

            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        return FirebaseAuth.getInstance(firebaseApp());
    }
}