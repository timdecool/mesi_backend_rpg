package com.ipi.mesi_backend_rpg.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_version_id", nullable = false)
    @JsonBackReference("module_version_block")
    private ModuleVersion module_version;

    private String title;
    private Integer order;
    private String type;

    //TODO : Relation Many to One to User
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Block() {
    }

    public Block(ModuleVersion module_version, String title, Integer order, String type, String createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.module_version = module_version;
        this.title = title;
        this.order = order;
        this.type = type;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ModuleVersion getModuleVersion() {
        return module_version;
    }

    public void setModuleVersion(ModuleVersion module_version) {
        this.module_version = module_version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return createdBy;
    }

    public void setUser(String user) {
        this.createdBy = user;
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
}
