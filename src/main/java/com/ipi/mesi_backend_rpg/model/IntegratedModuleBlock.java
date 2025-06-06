package com.ipi.mesi_backend_rpg.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class IntegratedModuleBlock extends Block {

    @ManyToOne
    @JsonBackReference("module-blocks")
    private Module module;
    
    public IntegratedModuleBlock(ModuleVersion moduleVersion, String title, Integer blockOrder, String type, User creator, Module module) {
        super(moduleVersion, title, blockOrder, type, creator);
        this.module = module;
    }

}
