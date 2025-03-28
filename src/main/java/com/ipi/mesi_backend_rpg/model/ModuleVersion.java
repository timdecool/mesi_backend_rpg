package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ModuleVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Module module;

    private int version;
    private boolean published;
    // TODO: ManyToOne GameSystem
    private String gameSystem;

    private String language;

    // TODO: ManyToOne User
    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;



    public ModuleVersion(Module module, int version, String createdBy, boolean published, String gameSystem, String language) {
        this();
        this.module = module;
        this.version = version;
        this.createdBy = createdBy;
        this.published = published;
        this.gameSystem = gameSystem;
        this.language = language;
    }

    public ModuleVersion() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getGameSystem() {
        return gameSystem;
    }

    public void setGameSystem(String gameSystem) {
        this.gameSystem = gameSystem;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
