package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ModuleVersionMapper {

    private final GameSystemRepository gameSystemRepository;

    public ModuleVersionDTO toDTO(ModuleVersion moduleVersion) {
        return new ModuleVersionDTO(
                moduleVersion.getId(),
                moduleVersion.getModule().getId(),
                moduleVersion.getVersion(),
                moduleVersion.getCreatedBy(),
                moduleVersion.getCreatedAt(),
                moduleVersion.getUpdatedAt(),
                moduleVersion.isPublished(),
                moduleVersion.getGameSystem().getId(),
                moduleVersion.getLanguage()
        );
    }

    public ModuleVersion toEntity(ModuleVersionDTO moduleVersionDTO) {

        GameSystem gameSystem = gameSystemRepository.findById(moduleVersionDTO.gameSystemId()).orElseThrow(() -> new IllegalArgumentException("Invalid game system id: " + moduleVersionDTO.gameSystemId()));

        ModuleVersion moduleVersion = new ModuleVersion();
        moduleVersion.setId(moduleVersionDTO.id());
        moduleVersion.setVersion(moduleVersionDTO.version());
        moduleVersion.setCreatedBy(moduleVersionDTO.createdBy());
        moduleVersion.setCreatedAt(moduleVersionDTO.createdAt() != null ? moduleVersionDTO.createdAt() : LocalDateTime.now());
        moduleVersion.setPublished(moduleVersionDTO.published());
        moduleVersion.setGameSystem(gameSystem);
        moduleVersion.setLanguage(moduleVersionDTO.language());
        return moduleVersion;
    }
}
