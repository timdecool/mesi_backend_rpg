package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("music")
public class MusicBlock extends Block {
    private String label;
    private String src;

    public MusicBlock(String label, String src, ModuleVersion moduleVersion, String title, Integer blockOrder, String type, User creator) {
        super(moduleVersion, title, blockOrder, type, creator);
        this.label = label;
        this.src = src;
    }

}
