package com.ipi.mesi_backend_rpg.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference("version-blocks")
    protected ModuleVersion moduleVersion;

    protected String title;
    protected Integer blockOrder;
    protected String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    protected User creator;

    @JsonFormat(pattern = "yyyy-MM-dd")
    protected LocalDate createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected LocalDate updatedAt;

    public Block() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    public Block(ModuleVersion moduleVersion, String title, Integer blockOrder, String type, User creator) {
        this();
        this.moduleVersion = moduleVersion;
        this.title = title;
        this.blockOrder = blockOrder;
        this.type = type;
        this.creator = creator;
    }
}
