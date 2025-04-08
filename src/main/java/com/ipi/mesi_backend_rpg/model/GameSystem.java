package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GameSystem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String name;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "gameSystem", fetch = FetchType.LAZY)
    private List<ModuleVersion> moduleVersions;

    public GameSystem(String name, LocalDate createdAt, LocalDate updatedAt) {
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
