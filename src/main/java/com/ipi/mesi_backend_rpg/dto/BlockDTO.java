package com.ipi.mesi_backend_rpg.dto;

import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BlockDTO(
        @NotNull Long id,
        @NotNull ModuleVersion module_version,
        @NotNull @NotBlank String title,
        @NotNull @NotBlank String type,
        @NotNull Integer blockOrder,
        @NotNull @NotBlank String createdBy
) {
}
