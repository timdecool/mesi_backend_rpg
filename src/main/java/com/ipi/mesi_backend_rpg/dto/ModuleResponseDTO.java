package com.ipi.mesi_backend_rpg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ipi.mesi_backend_rpg.model.ModuleAccess;

import java.time.LocalDateTime;
import java.util.List;

public record ModuleResponseDTO(
        Long id,
        String title,
        String description,
        Boolean isTemplate,
        String type,
        UserDTO creator,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,
        List<ModuleVersionDTO> versions,
        List<ModuleAccessDTO> accesses,
        List<TagDTO> tags,
        PictureDTO picture
) {
}
