package com.ipi.mesi_backend_rpg.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ParagraphBlockDTO.class, name = "paragraph"),
        @JsonSubTypes.Type(value = StatBlockDTO.class, name = "stats"),
        @JsonSubTypes.Type(value = IntegratedModuleBlockDTO.class, name="integratedModule")
        //TODO: Ajouter type de bloc ici
})
@Getter
@Setter
public abstract class BlockDTO {
    private Long id;
    @NotNull
    private Long moduleVersionId;
    @NotNull @NotBlank
    private String title;
    @NotNull
    private Integer blockOrder;
    @NotNull @NotBlank
    private String createdBy;

    public BlockDTO(Long id, Long moduleVersionId, String title, Integer blockOrder, String createdBy) {
        this.id = id;
        this.moduleVersionId = moduleVersionId;
        this.title = title;
        this.blockOrder = blockOrder;
        this.createdBy = createdBy;
    }

    public BlockDTO() {}
}
