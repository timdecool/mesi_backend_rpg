package com.ipi.mesi_backend_rpg.dto;

import java.util.List;

import com.ipi.mesi_backend_rpg.dto.simpleDTO.SimpleModuleDTO;

public record TagResponseDTO(
        Long id,
        String name,
        List<SimpleModuleDTO> modules) {
}
