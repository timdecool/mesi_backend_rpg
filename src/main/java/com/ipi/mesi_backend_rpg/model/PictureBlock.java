package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("picture")
public class PictureBlock extends Block {
    private String label;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Picture picture;

    public PictureBlock(String label, Picture picture, ModuleVersion moduleVersion, String title, Integer blockOrder, String type, User creator) {
        super(moduleVersion, title, blockOrder, type, creator);
        this.label = label;
        this.picture = picture;
    }

}
