package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long folderId;

    private Long userId;

    private String name;

    private Long parentFolder;

    public UserFolder(Long userId, String name, Long parentFolder) {
        this.userId = userId;
        this.name = name;
        this.parentFolder = parentFolder;
    }
}