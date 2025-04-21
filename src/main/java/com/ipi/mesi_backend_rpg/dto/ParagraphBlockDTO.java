package com.ipi.mesi_backend_rpg.dto;

public class ParagraphBlockDTO extends BlockDTO {
    private String paragraph;
    private String style;

    public ParagraphBlockDTO() {}

    public ParagraphBlockDTO(String paragraph, String style, Long id, Long moduleVersionId, String title, Integer blockOrder, UserDTO creator) {
        super(id, moduleVersionId, title, blockOrder, creator);
        this.paragraph = paragraph;
        this.style = style;
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
