package com.ipi.mesi_backend_rpg.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "title required")
    @NotBlank(message = "title empty")
    private String title;

    @NotNull(message = "description required")
    @NotBlank(message = "description required")
    private String description;

    //TODO: Make join on User Table
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isTemplate;

    //TODO: Make join on ModuleType Enum
    private String type;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            })
    @JoinTable(name = "module_tag",
            joinColumns = {@JoinColumn(name = "module_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")})
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "module", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ModuleVersion> versions = new ArrayList<>();

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
    @JsonManagedReference("module_module_access")
    private List<ModuleAccess> accesses;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
    private List<IntegratedModuleBlock> moduleBlocks;

    public Module(String title, String description, String createdBy, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isTemplate, String type) {
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isTemplate = isTemplate;
        this.type = type;
    }

    public void addTag(Tag tag) {
        this.getTags().add(tag);
    }

    public void removeTag(Tag tag) {
        this.getTags().remove(tag);
    }

    public void addVersion(ModuleVersion version) {
        this.getVersions().add(version);
    }

}
