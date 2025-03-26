package com.ipi.mesi_backend_rpg.dto;

import jakarta.validation.constraints.NotNull;

public record ModuleAccessDTO(
        Integer id,
        Long moduleId,
        Integer userId,
        @NotNull boolean canView,
        @NotNull boolean canEdit,
        @NotNull boolean canPublish,
        @NotNull boolean canInvite
) {
}
