package com.ipi.mesi_backend_rpg.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("module-savedModules")
    private Module module;

    private Long moduleVersionId;

    private Long folderId;

    private String alias;

    public UserSavedModule(Long userId, Module module, Long moduleVersionId, Long folderId, String alias) {
        this.userId = userId;
        this.module = module;
        this.moduleVersionId = moduleVersionId;
        this.folderId = folderId;
        this.alias = alias;
    }

    public Long getModuleId() {
        return this.module != null ? this.module.getId() : null;
    }

    public void setModuleId(Long moduleId) {
    }
}