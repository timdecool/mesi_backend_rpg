package com.ipi.mesi_backend_rpg.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ParagraphBlockDTO.class, name = "paragraph")
        //TODO: Ajouter type de bloc ici
})
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull Long getModuleVersionId() {
        return moduleVersionId;
    }

    public void setModuleVersionId(@NotNull Long moduleVersionId) {
        this.moduleVersionId = moduleVersionId;
    }

    public @NotNull @NotBlank String getTitle() {
        return title;
    }

    public void setTitle(@NotNull @NotBlank String title) {
        this.title = title;
    }

    public @NotNull Integer getBlockOrder() {
        return blockOrder;
    }

    public void setBlockOrder(@NotNull Integer blockOrder) {
        this.blockOrder = blockOrder;
    }

    public @NotNull @NotBlank String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(@NotNull @NotBlank String createdBy) {
        this.createdBy = createdBy;
    }
}
