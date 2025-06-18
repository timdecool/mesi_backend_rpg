package com.ipi.mesi_backend_rpg.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ModuleRatingDTO(
        Long id,
        @NotNull
        Long moduleId,
        Long moduleVersionId,
        UserDTO user,
        @NotNull()
        int rating,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
}