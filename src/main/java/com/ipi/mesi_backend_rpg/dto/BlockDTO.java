package com.ipi.mesi_backend_rpg.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ParagraphBlockDTO.class, name = "paragraph"),
    @JsonSubTypes.Type(value = MusicBlockDTO.class, name = "music"),
    @JsonSubTypes.Type(value = StatBlockDTO.class, name = "stat"),
    @JsonSubTypes.Type(value = IntegratedModuleBlockDTO.class, name = "module")
})
@Setter
@Getter
public abstract class BlockDTO {
    private Long id;
    @NotNull
    private Long moduleVersionId;
    @NotNull
    @NotBlank
    private String title;
    @NotNull
    private Integer blockOrder;
    @NotNull
    private UserDTO creator;
    @NotNull
    private String type;
    private Long entityVersion;
    private LocalDateTime lastModified;

    public BlockDTO(Long id, Long moduleVersionId, String title, Integer blockOrder, UserDTO creator) {
        this.id = id;
        this.moduleVersionId = moduleVersionId;
        this.title = title;
        this.blockOrder = blockOrder;
        this.creator = creator;
    }

    public BlockDTO() {
    }

}
