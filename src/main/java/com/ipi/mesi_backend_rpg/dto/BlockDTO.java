package com.ipi.mesi_backend_rpg.dto;

import com.ipi.mesi_backend_rpg.model.ModuleVersion;

public record BlockDTO(
        Long id,
        ModuleVersion module_version,
        String title,
        String type,
        Integer blockOrder,
        String createdBy
) {
}
