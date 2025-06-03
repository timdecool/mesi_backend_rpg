package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
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
        private final UserRepository userRepository;
        private final UserMapper userMapper;
        private final BlockMapper blockMapper;

        public ModuleVersionDTO toDTO(ModuleVersion moduleVersion) {
                List<BlockDTO> blockDTOs = new ArrayList<>();
                if (moduleVersion.getBlocks() != null) {
                        blockDTOs = moduleVersion.getBlocks().stream()
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
                                blockDTOs,
                                moduleVersion.getLversion(), // ✅ Utiliser lversion pour entityVersion
                                moduleVersion.getLastModified());
        }

        public ModuleVersion toEntity(ModuleVersionDTO moduleVersionDTO) {
                List<Block> blocks = new ArrayList<>();
                if (moduleVersionDTO.blocks() != null) {
                        blocks = moduleVersionDTO.blocks().stream()
                                        .map(blockMapper::toEntity)
                                        .collect(Collectors.toList());
                }

                GameSystem gameSystem = gameSystemRepository.findById(moduleVersionDTO.gameSystemId()).orElseThrow(
                                () -> new IllegalArgumentException(
                                                "Invalid game system id: " + moduleVersionDTO.gameSystemId()));

                ModuleVersion moduleVersion = new ModuleVersion();
                moduleVersion.setId(moduleVersionDTO.id());

                // ✅ Gérer correctement la version JPA
                if (moduleVersionDTO.entityVersion() != null) {
                        moduleVersion.setLversion(moduleVersionDTO.entityVersion());
                }

                moduleVersion.setVersion(moduleVersionDTO.version());
                moduleVersion.setCreator(userRepository.findById(moduleVersionDTO.creator().id()).orElseThrow(
                                () -> new IllegalArgumentException(
                                                "Invalid user id: " + moduleVersionDTO.creator().id())));
                moduleVersion.setCreatedAt(
                                moduleVersionDTO.createdAt() != null ? moduleVersionDTO.createdAt()
                                                : LocalDateTime.now());
                moduleVersion.setPublished(moduleVersionDTO.published());
                moduleVersion.setGameSystem(gameSystem);
                moduleVersion.setLanguage(moduleVersionDTO.language());
                moduleVersion.setBlocks(blocks);
                return moduleVersion;
        }
}