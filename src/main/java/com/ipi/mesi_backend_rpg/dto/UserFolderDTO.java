package com.ipi.mesi_backend_rpg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserFolderDTO (
    @NotNull @NotBlank(message = "folder_id should not be empty")
    Long folder_id,

    @NotNull @NotBlank(message = "user_id should not be empty")
    Long user_id,

    String name,

    @NotNull @NotBlank(message = "parent_folder should not be empty")
    Long parent_folder
) {}
