package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
public class FileMetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uniqueId;
    private String objectName;
    private LocalDateTime uploadDate;
    private String contentType;
    private String publicUrl;

    public FileMetaData(String uniqueId, String objectName, LocalDateTime uploadDate, String contentType, String publicUrl) {
        this.uniqueId = uniqueId;
        this.objectName = objectName;
        this.uploadDate = uploadDate;
        this.contentType = contentType;
        this.publicUrl = publicUrl;
    }
}