package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ModuleRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY)
    private ModuleVersion moduleVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private int rating;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ModuleRating(Module module, ModuleVersion moduleVersion, User user, int rating) {
        this();
        this.module = module;
        this.moduleVersion = moduleVersion;
        this.user = user;
        this.rating = rating;
    }

    public ModuleRating() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
