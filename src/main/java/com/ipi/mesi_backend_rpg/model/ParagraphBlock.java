package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.Entity;

@Entity
public class ParagraphBlock extends Block {

    private String paragraph;
    private String style;

    public ParagraphBlock(String paragraph, String style) {
        this.paragraph = paragraph;
        this.style = style;
    }

    public ParagraphBlock(ModuleVersion moduleVersion, String title, Integer blockOrder, String type, String createdBy, String paragraph, String style) {
        super(moduleVersion, title, blockOrder, type, createdBy);
        this.paragraph = paragraph;
        this.style = style;
    }

    public ParagraphBlock() {

    }

    public String getParagraph() {
        return paragraph;
    }

    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
}
