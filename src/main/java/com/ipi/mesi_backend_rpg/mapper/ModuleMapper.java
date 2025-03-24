package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ModuleMapper {

    public ModuleResponseDTO toDTO(Module module) {
        return new ModuleResponseDTO(
                module.getId(),
                module.getTitle(),
                module.getDescription(),
                module.getTemplate(),
                module.getType(),
                module.getPicture(),
                module.getCreatedBy(),
                module.getCreatedAt(),
                module.getUpdatedAt()
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
                moduleRequestDTO.type(),
                moduleRequestDTO.picture()
        );
    }
}
