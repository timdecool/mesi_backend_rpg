package com.ipi.mesi_backend_rpg.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import com.ipi.mesi_backend_rpg.dto.FileMetaDataDTO;
import com.ipi.mesi_backend_rpg.mapper.FileMetaDataMapper;
import com.ipi.mesi_backend_rpg.model.FileMetaData;
import com.ipi.mesi_backend_rpg.repository.FileMetaDataRepository;

@Service
public class FileStorageService {

    private final Storage storage;
    private final FileMetaDataRepository repo;
    private final FileMetaDataMapper fileMetaDataMapper;
    private static final Logger logger = Logger.getLogger(FileStorageService.class.getName());

    @Value("${app.file.storage.enabled:true}")
    private boolean fileStorageEnabled;

    public FileStorageService(FileMetaDataRepository repo, FileMetaDataMapper fileMetaDataMapper) throws IOException {
        this.repo = repo;
        this.fileMetaDataMapper = fileMetaDataMapper;

        InputStream serviceAccount = null;
        
        // Tenter plusieurs emplacements pour trouver le fichier Firebase
        try {
            // 1. Essayer d'abord le classpath (méthode originale)
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            serviceAccount = resource.getInputStream();
            logger.info("Firebase credentials loaded from classpath");
        } catch (IOException e) {
            // 2. Essayer la propriété système
            String firebasePath = System.getProperty("firebase.service.account.path");
            if (firebasePath != null) {
                File file = new File(firebasePath);
                if (file.exists()) {
                    serviceAccount = new FileInputStream(file);
                    logger.log(Level.INFO, "Firebase credentials loaded from system property path: {0}", firebasePath);
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
                        logger.log(Level.INFO, "Firebase credentials loaded from path: {0}", path);
                        break;
                    }
                }
            }
            
            // 4. En dernier recours, utiliser un fichier vide
            if (serviceAccount == null) {
                logger.warning("No Firebase credentials found, using empty configuration");
                // Créer un service Firebase vide (attention, cela peut causer des problèmes d'API)
                serviceAccount = new ByteArrayInputStream("{}".getBytes());
            }
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

        this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (!fileStorageEnabled) {
            throw new IllegalStateException("File storage is disabled");
        }
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty. Please upload a valid file.");
        }

        String uniqueID = UUID.randomUUID().toString();
        String objectName = uniqueID + "_" + file.getOriginalFilename();

        String bucketName = "jdr-mesi.firebasestorage.app";
        BlobId blobId = BlobId.of(bucketName, objectName);

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        Blob blob = storage.create(blobInfo, file.getBytes());
        String publicUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucketName,
                objectName.replace("/", "%2F"));

        FileMetaData metaData = new FileMetaData();
        metaData.setUniqueId(uniqueID);
        metaData.setObjectName(objectName);
        metaData.setUploadDate(LocalDateTime.now());
        metaData.setContentType(file.getContentType());
        metaData.setPublicUrl(publicUrl);

        repo.save(metaData);

        return publicUrl;
    }

    public FileMetaDataDTO retrieveFile(String fileId) {
        if (!fileStorageEnabled) {
            throw new IllegalStateException("File storage is disabled");
        }
        
        FileMetaData fileMetadata = repo.findByUniqueId(fileId);

        if (fileMetadata == null) {
            throw new IllegalArgumentException("No file found with the given ID: " + fileId);
        }

        return fileMetaDataMapper.toDTO(fileMetadata);
    }

    public void deleteFile(String fileId) {
        if (!fileStorageEnabled) {
            throw new IllegalStateException("File storage is disabled");
        }
        
        repo.delete(repo.findByUniqueId(fileId));
    }
}