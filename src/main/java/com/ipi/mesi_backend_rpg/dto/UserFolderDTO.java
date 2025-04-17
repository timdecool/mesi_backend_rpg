package com.ipi.mesi_backend_rpg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserFolderDTO(
        Long folderId,
        @NotNull @NotBlank(message = "userId ne doit pas être null") Long userId,
        String name,
        Long parentFolder) {
}