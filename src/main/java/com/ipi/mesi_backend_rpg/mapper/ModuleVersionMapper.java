package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleVersionMapper {

    private final GameSystemRepository gameSystemRepository;
    private final UserMapper userMapper;
    private final BlockMapper blockMapper;

    public ModuleVersionDTO toDTO(ModuleVersion moduleVersion) {
        List<BlockDTO> blockDTOs = new ArrayList<>();
        if (moduleVersion.getBlocks() != null) {
        blockDTOs = moduleVersion.getBlocks().stream()
                        .sorted((b1, b2) -> {
                            if (b1.getBlockOrder() == null && b2.getBlockOrder() == null) return 0;
                            if (b1.getBlockOrder() == null) return 1;
                            if (b2.getBlockOrder() == null) return -1;
                            return b1.getBlockOrder().compareTo(b2.getBlockOrder());
                        })
                        .map(blockMapper::toDTO)
                        .collect(Collectors.toList());
        }

        return new ModuleVersionDTO(
                moduleVersion.getId(),
                moduleVersion.getModule().getId(),
                moduleVersion.getVersion(),
                userMapper.toDTO(moduleVersion.getCreator()),
                moduleVersion.getCreatedAt(),
                moduleVersion.getUpdatedAt(),
                moduleVersion.isPublished(),
                moduleVersion.getGameSystem().getId(),
                moduleVersion.getLanguage(),
                blockDTOs);
    }

    public ModuleVersion toEntity(ModuleVersionDTO moduleVersionDTO) {
        List<Block> blocks = new ArrayList<>();
        if (moduleVersionDTO.blocks() != null) {
        blocks = moduleVersionDTO.blocks().stream()
                        .map(blockMapper::toEntity)
                        .collect(Collectors.toList());
        }

        GameSystem gameSystem = gameSystemRepository.findById(moduleVersionDTO.gameSystemId()).orElseThrow(
                () -> new IllegalArgumentException("Invalid game system id: " + moduleVersionDTO.gameSystemId()));

        ModuleVersion moduleVersion = new ModuleVersion();
        moduleVersion.setId(moduleVersionDTO.id());
        moduleVersion.setVersion(moduleVersionDTO.version());
        moduleVersion.setCreator(null);
        moduleVersion.setCreatedAt(
                moduleVersionDTO.createdAt() != null ? moduleVersionDTO.createdAt() : LocalDateTime.now());
        moduleVersion.setPublished(moduleVersionDTO.published());
        moduleVersion.setGameSystem(gameSystem);
        moduleVersion.setLanguage(moduleVersionDTO.language());
        moduleVersion.setBlocks(blocks);
        return moduleVersion;
    }
}
