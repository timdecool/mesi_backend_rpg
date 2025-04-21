package com.ipi.mesi_backend_rpg.dto;

import jakarta.validation.constraints.NotNull;

public record UserSavedModuleDTO(
                Long savedModuleId,
                @NotNull(message = "userId ne doit pas être null") Long userId,
                @NotNull(message = "moduleId ne doit pas être null") Long moduleId,
                @NotNull(message = "moduleVersionId ne doit pas être null") Long moduleVersionId,
                Long folderId,
                String alias) {
}