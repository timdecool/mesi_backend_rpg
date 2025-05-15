package com.ipi.mesi_backend_rpg.dto;

import java.util.List;

public record ModuleResponseSummaryDTO(
        Long id,
        String title,
        String description,
        UserDTO creator,
        List<ModuleVersionDTO> versions,
        PictureDTO picture
) {
}
