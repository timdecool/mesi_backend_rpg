package com.ipi.mesi_backend_rpg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record ModuleVersionDTO(
        Long id,
        Long moduleId,
        Integer version,
        UserDTO creator,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,
        Boolean published,
        Long gameSystemId,
        String language,
        List<BlockDTO> blocks,
        Long entityVersion,
        LocalDateTime lastModified
) {
}
