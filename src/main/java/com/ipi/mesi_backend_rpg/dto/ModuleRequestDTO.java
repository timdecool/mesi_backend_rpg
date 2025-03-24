package com.ipi.mesi_backend_rpg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ModuleRequestDTO(
        @NotNull @NotBlank String title,
        @NotNull @NotBlank String description,
        @NotNull Boolean isTemplate,
        @NotNull String type,
        @NotNull String picture
) {
}


