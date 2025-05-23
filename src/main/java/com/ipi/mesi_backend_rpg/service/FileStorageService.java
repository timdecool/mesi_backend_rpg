package com.ipi.mesi_backend_rpg.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public FileStorageService(FileMetaDataRepository repo, FileMetaDataMapper fileMetaDataMapper) throws IOException {
        this.repo = repo;
        this.fileMetaDataMapper = fileMetaDataMapper;

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
        FileMetaData fileMetadata = repo.findByUniqueId(fileId);

        if (fileMetadata == null) {
            throw new IllegalArgumentException("No file found with the given ID: " + fileId);
        }

        return fileMetaDataMapper.toDTO(fileMetadata);
    }

    public void deleteFile(String fileId) {
        repo.delete(repo.findByUniqueId(fileId));
    }


}
