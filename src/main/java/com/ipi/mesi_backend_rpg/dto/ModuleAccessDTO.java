package com.ipi.mesi_backend_rpg.dto;

import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.User;
import jakarta.validation.constraints.NotNull;

public record ModuleAccessDTO(
        Integer id,
        Module module,
        User user,
        @NotNull boolean canView,
        @NotNull boolean canEdit,
        @NotNull boolean canPublish,
        @NotNull boolean canInvite
) {
}
