package com.ipi.mesi_backend_rpg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BlockDTO(
        Long id,
        @NotNull Long moduleVersionId,
        @NotNull @NotBlank String title,
        @NotNull @NotBlank String type,
        @NotNull Integer blockOrder,
        @NotNull @NotBlank String createdBy
) {
}
