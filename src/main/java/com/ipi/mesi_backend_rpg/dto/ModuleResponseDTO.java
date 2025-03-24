package com.ipi.mesi_backend_rpg.dto;

import java.time.LocalDateTime;

public record ModuleResponseDTO(
        Long id,
        String title,
        String description,
        Boolean isTemplate,
        String type,
        String picture,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
)
{
}
