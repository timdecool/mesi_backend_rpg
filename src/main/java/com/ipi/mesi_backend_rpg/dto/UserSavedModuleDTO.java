package com.ipi.mesi_backend_rpg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserSavedModuleDTO(
        @NotNull @NotBlank(message = "saved_module_id should not be null") Long saved_module_id,

        @NotNull @NotBlank(message = "user_id should not be null") Long user_id,

        @NotNull @NotBlank(message = "module_id should not be null") Long module_id,

        @NotNull @NotBlank(message = "module_version_id should not be null") Long module_version_id,

        Long folder_id,

        String alias) {

}
