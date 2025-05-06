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
@DiscriminatorValue("paragraph")
public class ParagraphBlock extends Block {

    private String paragraph;
    private String style;

    public ParagraphBlock(String paragraph, String style) {
        this.paragraph = paragraph;
        this.style = style;
    }

    public ParagraphBlock(ModuleVersion moduleVersion, String title, Integer blockOrder, String type, User creator, String paragraph, String style) {
        super(moduleVersion, title, blockOrder, type, creator);
        this.paragraph = paragraph;
        this.style = style;
    }
    
}
