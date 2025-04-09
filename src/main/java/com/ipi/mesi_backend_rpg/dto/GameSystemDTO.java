package com.ipi.mesi_backend_rpg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GameSystemDTO(
        Long id,
        @NotNull
        @NotBlank(message = "Title is empty")
        String name,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate updatedAt
) {
}
