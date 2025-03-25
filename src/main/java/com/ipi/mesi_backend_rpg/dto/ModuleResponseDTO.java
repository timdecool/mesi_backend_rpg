package com.ipi.mesi_backend_rpg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ModuleResponseDTO(
        Long id,
        String title,
        String description,
        Boolean isTemplate,
        String type,
        String picture,
        String createdBy,

        @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")

        LocalDateTime createdAt,
        @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")

        LocalDateTime updatedAt
)
{
}
