package com.ipi.mesi_backend_rpg.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class ModuleBlock {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Block block;


    @ManyToOne
    @JsonBackReference("module_module_block")
    private Module module;

    @ManyToOne
    @JsonBackReference("module_version_module_block")
    private ModuleVersion moduleVersion;

    public ModuleBlock() {
    }

    public ModuleBlock(Block block, Module module, ModuleVersion moduleVersion) {
        this.block = block;
        this.module = module;
        this.moduleVersion = moduleVersion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public ModuleVersion getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(ModuleVersion moduleVersion) {
        this.moduleVersion = moduleVersion;
    }
}
