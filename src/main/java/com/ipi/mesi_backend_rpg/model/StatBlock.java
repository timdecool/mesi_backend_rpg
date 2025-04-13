package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class StatBlock extends Block {

    private String statRules;
    private String statValues;

    public StatBlock(ModuleVersion moduleVersion, String title, Integer blockOrder, String type, String createdBy, String statRules, String statValues) {
        super(moduleVersion, title, blockOrder, type, createdBy);
        this.statRules = statRules;
        this.statValues = statValues;
    }

}
