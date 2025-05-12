package com.ipi.mesi_backend_rpg.dto;

import java.util.List;

public record TagRequestDTO(
        String name,
        List<Long> moduleIds) {
}
