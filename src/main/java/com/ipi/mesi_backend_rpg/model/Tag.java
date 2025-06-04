package com.ipi.mesi_backend_rpg.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "tag name mandatory")
    @NotBlank(message = "tag name empty")
    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, mappedBy = "tags")
    @JsonIgnore
    private List<Module> modules;

    public Tag(String name, List<Module> modules) {
        this.name = name;
        this.modules = modules;
    }

    public void addToModule(Module module) {
        if (this.modules == null) {
            this.modules = new ArrayList<>();
        }
        if (!this.modules.contains(module)) {
            this.modules.add(module);
        }
        if (module.getTags() == null) {
            module.setTags(new ArrayList<>());
        }
        if (!module.getTags().contains(this)) {
            module.getTags().add(this);
        }
    }

}
