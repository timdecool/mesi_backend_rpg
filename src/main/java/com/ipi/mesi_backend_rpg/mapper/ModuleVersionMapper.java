package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ModuleVersionMapper {

    private final GameSystemRepository gameSystemRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public ModuleVersionDTO toDTO(ModuleVersion moduleVersion) {
        return new ModuleVersionDTO(
                moduleVersion.getId(),
                moduleVersion.getModule().getId(),
                moduleVersion.getVersion(),
                userMapper.toDTO(moduleVersion.getCreator()),
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
        moduleVersion.setCreator(userRepository.findById(moduleVersionDTO.creator().id()).orElseThrow(() -> new IllegalArgumentException("Invalid user id: " + moduleVersionDTO.creator().id())));
        moduleVersion.setCreatedAt(moduleVersionDTO.createdAt() != null ? moduleVersionDTO.createdAt() : LocalDateTime.now());
        moduleVersion.setPublished(moduleVersionDTO.published());
        moduleVersion.setGameSystem(gameSystem);
        moduleVersion.setLanguage(moduleVersionDTO.language());
        return moduleVersion;
    }
}
