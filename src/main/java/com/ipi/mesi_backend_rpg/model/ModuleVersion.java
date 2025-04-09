package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class ModuleVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Module module;

    private int version;
    private boolean published;
    private String language;

    // TODO: ManyToOne User
    private String createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "moduleVersion", fetch = FetchType.LAZY)
    private List<Block> blocks;

    @ManyToOne(fetch = FetchType.LAZY)
    private GameSystem gameSystem;

    public ModuleVersion(Module module, int version, String createdBy, boolean published, GameSystem gameSystem, String language) {
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
}
