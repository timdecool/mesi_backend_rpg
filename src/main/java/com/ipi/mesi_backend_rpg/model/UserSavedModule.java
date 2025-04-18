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
public class UserSavedModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long savedModuleId;

    private Long userId;

    private Long moduleId;

    private Long moduleVersionId;

    private Long folderId;

    private String alias;

    public UserSavedModule(Long userId, Long moduleId, Long moduleVersionId, Long folderId, String alias) {
        this.userId = userId;
        this.moduleId = moduleId;
        this.moduleVersionId = moduleVersionId;
        this.folderId = folderId;
        this.alias = alias;
    }
}