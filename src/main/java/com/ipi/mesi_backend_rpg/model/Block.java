package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    protected ModuleVersion moduleVersion;

    protected String title;
    protected Integer blockOrder;
    protected String type;

    @ManyToOne(fetch = FetchType.LAZY)
    protected User creator;

    protected LocalDate createdAt;
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
