package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import org.springframework.stereotype.Service;

@Service
public class ModuleMapperService {

    public ModuleResponseDTO toDTO(Module module) {
        return new ModuleResponseDTO(
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
}
