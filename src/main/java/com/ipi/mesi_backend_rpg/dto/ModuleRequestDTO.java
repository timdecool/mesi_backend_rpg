package com.ipi.mesi_backend_rpg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ModuleRequestDTO(
        @NotNull(message = "Title is not provided.")
        @NotBlank(message = "Title is empty.")
        String title,
        @NotNull(message = "Description is not provided.")
        String description,
        @NotNull(message = "Template status is not provided.")
        Boolean isTemplate,
        @NotNull(message = "Type is not provided.")
        String type,
        @NotNull(message = "Creator is not provided.")
        UserDTO creator,
        PictureDTO picture
) {
}


