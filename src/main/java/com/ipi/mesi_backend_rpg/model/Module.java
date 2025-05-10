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

    @ManyToOne(fetch = FetchType.LAZY)
    private User creator;

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

    @OneToMany(mappedBy = "module", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("module_module_access")
    private List<ModuleAccess> accesses = new ArrayList<>();

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
    private List<IntegratedModuleBlock> moduleBlocks;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ModuleComment> comments;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Picture picture;

    public Module(String title, String description, User creator, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isTemplate, String type) {
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isTemplate = isTemplate;
        this.type = type;

    }
    public Module(String title, String description, User creator, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isTemplate, String type, Picture picture) {
        this(title, description, creator, createdAt, updatedAt, isTemplate, type);
        this.picture = picture;
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

    public void addAccess(ModuleAccess access) { this.getAccesses().add(access); }
    public void removeAccess(ModuleAccess access) { this.getAccesses().remove(access); }


}
