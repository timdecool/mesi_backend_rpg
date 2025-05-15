package com.ipi.mesi_backend_rpg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record FileMetaDataDTO(
        @NotNull @NotBlank String uniqueId,
        @NotNull @NotBlank String objectName,
        @NotNull @NotBlank @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime uploadDate,
        @NotNull @NotBlank String contentType,
        @NotNull @NotBlank String publicUrl
) {
}
