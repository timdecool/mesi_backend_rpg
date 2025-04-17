package com.ipi.mesi_backend_rpg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserSavedModuleDTO(
                Long savedModuleId,
                @NotNull @NotBlank(message = "userId ne doit pas être null") Long userId,
                @NotNull @NotBlank(message = "moduleId ne doit pas être null") Long moduleId,
                @NotNull @NotBlank(message = "moduleVersionId ne doit pas être null") Long moduleVersionId,
                Long folderId,
                String alias) {
}