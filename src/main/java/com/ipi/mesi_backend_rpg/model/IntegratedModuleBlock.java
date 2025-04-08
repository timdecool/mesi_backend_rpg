package com.ipi.mesi_backend_rpg.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class IntegratedModuleBlock extends Block {

    @ManyToOne
    @JsonBackReference("module_module_block")
    private Module module;


    public IntegratedModuleBlock(ModuleVersion moduleVersion, String title, Integer blockOrder, String type, String createdBy, Module module) {
        super(moduleVersion, title, blockOrder, type, createdBy);
        this.module = module;
    }

    public IntegratedModuleBlock() {
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }
}
