package com.ipi.mesi_backend_rpg.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParagraphBlockDTO extends BlockDTO {
    private String paragraph;
    private String style;

    public ParagraphBlockDTO() {}

    public ParagraphBlockDTO(String paragraph, String style, Long id, Long moduleVersionId, String title, Integer blockOrder, String createdBy) {
        super(id, moduleVersionId, title, blockOrder, createdBy);
        this.paragraph = paragraph;
        this.style = style;
    }
}
