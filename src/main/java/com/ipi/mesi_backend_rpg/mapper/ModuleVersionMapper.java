package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ModuleVersionMapper {

    public ModuleVersionDTO toDTO(ModuleVersion moduleVersion) {
        return new ModuleVersionDTO(
                moduleVersion.getId(),
                moduleVersion.getModule().getId(),
                moduleVersion.getVersion(),
                moduleVersion.getCreatedBy(),
                moduleVersion.getCreatedAt(),
                moduleVersion.getUpdatedAt(),
                moduleVersion.isPublished(),
                moduleVersion.getGameSystem(),
                moduleVersion.getLanguage()
        );

    }

    public ModuleVersion toEntity(ModuleVersionDTO moduleVersionDTO) {
        ModuleVersion moduleVersion = new ModuleVersion();
        moduleVersion.setId(moduleVersionDTO.id());
        moduleVersion.setVersion(moduleVersionDTO.version());
        moduleVersion.setCreatedBy(moduleVersionDTO.createdBy());
        moduleVersion.setCreatedAt(moduleVersionDTO.createdAt() != null ? moduleVersionDTO.createdAt() : LocalDateTime.now());
        moduleVersion.setPublished(moduleVersionDTO.published());
        moduleVersion.setGameSystem(moduleVersionDTO.gameSystem());
        moduleVersion.setLanguage(moduleVersionDTO.language());
        return moduleVersion;
    }
}
