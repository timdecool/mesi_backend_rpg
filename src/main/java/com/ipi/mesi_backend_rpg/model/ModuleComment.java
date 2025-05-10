package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ModuleComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY)
    private ModuleVersion moduleVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String comment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ModuleComment(Module module, ModuleVersion moduleVersion, User user, String comment) {
        this();
        this.module = module;
        this.moduleVersion = moduleVersion;
        this.user = user;
        this.comment = comment;
    }

    public ModuleComment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
