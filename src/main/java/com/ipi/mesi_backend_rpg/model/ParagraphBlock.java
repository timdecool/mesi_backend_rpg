package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ParagraphBlock extends Block {

    @Lob
    @Column(columnDefinition="TEXT")
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
