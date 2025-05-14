package com.ipi.mesi_backend_rpg.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.common.collect.Lists;
import com.ipi.mesi_backend_rpg.model.FileMetaData;
import com.ipi.mesi_backend_rpg.repository.FileMetaDataRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Storage storage;
    private final FileMetaDataRepository repo;
    private final String bucketName = "jdr-mesi.firebasestorage.app";

    public FileStorageService(FileMetaDataRepository repo) throws IOException {
        this.repo = repo;

        ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
        InputStream serviceAccount = resource.getInputStream();

        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

        this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty. Please upload a valid file.");
        }

        String uniqueID = UUID.randomUUID().toString();
        String objectName = uniqueID + "_" + file.getOriginalFilename();

        BlobId blobId = BlobId.of(bucketName, objectName);

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        storage.create(blobInfo, file.getBytes());

        FileMetaData metaData = new FileMetaData();
        metaData.setUniqueId(uniqueID);
        metaData.setObjectName(objectName);
        metaData.setUploadDate(LocalDateTime.now());

        repo.save(metaData);

        return uniqueID;
    }

    public FileResponse retrieveFile(String fileId) {
        FileMetaData fileMetadata = repo.findByUniqueId(fileId);

        if (fileMetadata == null) {
            throw new IllegalArgumentException("No file found with the given ID: " + fileId);
        }

        String objectName = fileMetadata.getObjectName();
        BlobId blobId = BlobId.of(bucketName, objectName);
        Blob blob = storage.get(blobId);

        if (blob == null || !blob.exists()) {
            throw new IllegalArgumentException("No file found with the given ID: " + fileId);
        }

        FileResponse fileResponse = new FileResponse(objectName, blob.getContent());
        return fileResponse;
    }

    public class FileResponse {
        private final String fileName;
        private final byte[] fileContent;

        public FileResponse(String fileName, byte[] fileContent) {
            this.fileName = fileName;
            this.fileContent = fileContent;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getFileContent() {
            return fileContent;
        }
    }

}
