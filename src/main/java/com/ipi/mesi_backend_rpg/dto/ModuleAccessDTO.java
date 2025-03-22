package com.ipi.mesi_backend_rpg.dto;

import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.User;

public record ModuleAccessDTO(
        Integer id,
        Module module,
        User user,
        boolean canView,
        boolean canEdit,
        boolean canPublish,
        boolean canInvite
) {
}
