package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ModuleMapper {

    private final ModuleVersionMapper moduleVersionMapper;
    private final TagMapper tagMapper;

    public ModuleMapper(ModuleVersionMapper moduleVersionMapper, TagMapper tagMapper) {
        this.moduleVersionMapper = moduleVersionMapper;
        this.tagMapper = tagMapper;
    }

    public ModuleResponseDTO toDTO(Module module) {
        return new ModuleResponseDTO(
                module.getId(),
                module.getTitle(),
                module.getDescription(),
                module.getIsTemplate(),
                module.getType(),
                module.getCreatedBy(),
                module.getCreatedAt(),
                module.getUpdatedAt(),
                module.getVersions().stream().map(moduleVersionMapper::toDTO).toList(),
                module.getTags().stream().map(tagMapper::toDTO).toList()
        );
    }

    public Module toEntity(ModuleRequestDTO moduleRequestDTO) {
        return new Module(
                moduleRequestDTO.title(),
                moduleRequestDTO.description(),
                "author",
                LocalDateTime.now(),
                LocalDateTime.now(),
                moduleRequestDTO.isTemplate(),
                moduleRequestDTO.type()
        );
    }
}
