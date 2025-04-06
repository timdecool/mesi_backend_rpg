package com.ipi.mesi_backend_rpg.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    protected ModuleVersion moduleVersion;

    protected String title;
    protected Integer blockOrder;
    protected String type;

    //TODO : Relation Many to One to User
    protected String createdBy;
    protected LocalDate createdAt;
    protected LocalDate updatedAt;

    public Block() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    public Block(ModuleVersion moduleVersion, String title, Integer blockOrder, String type, String createdBy) {
        this();
        this.moduleVersion = moduleVersion;
        this.title = title;
        this.blockOrder = blockOrder;
        this.type = type;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ModuleVersion getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(ModuleVersion moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getBlockOrder() {
        return blockOrder;
    }

    public void setBlockOrder(Integer blockOrder) {
        this.blockOrder = blockOrder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }
}
