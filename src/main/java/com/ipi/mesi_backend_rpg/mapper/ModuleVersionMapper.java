package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.IntegratedModuleBlockDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.dto.MusicBlockDTO;
import com.ipi.mesi_backend_rpg.dto.ParagraphBlockDTO;
import com.ipi.mesi_backend_rpg.dto.StatBlockDTO;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.model.IntegratedModuleBlock;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.model.MusicBlock;
import com.ipi.mesi_backend_rpg.model.ParagraphBlock;
import com.ipi.mesi_backend_rpg.model.StatBlock;
import com.ipi.mesi_backend_rpg.repository.BlockRepository;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleVersionMapper {

    private final GameSystemRepository gameSystemRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BlockRepository blockRepository;

    public ModuleVersionDTO toDTO(ModuleVersion moduleVersion) {
        List<BlockDTO> blockDTOs = blockRepository.findAllByModuleVersion(moduleVersion)
                .stream()
                .map(this::mapBlockToDTO)
                .collect(Collectors.toList());

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

    private BlockDTO mapBlockToDTO(Block block) {
        if (block instanceof ParagraphBlock paragraphBlock) {
            return new ParagraphBlockDTO(
                    paragraphBlock.getParagraph(),
                    paragraphBlock.getStyle(),
                    paragraphBlock.getId(),
                    paragraphBlock.getModuleVersion().getId(),
                    paragraphBlock.getTitle(),
                    paragraphBlock.getBlockOrder(),
                    userMapper.toDTO(paragraphBlock.getCreator()));
        } else if (block instanceof MusicBlock musicBlock) {
            return new MusicBlockDTO(
                    musicBlock.getLabel(),
                    musicBlock.getSrc(),
                    musicBlock.getId(),
                    musicBlock.getModuleVersion().getId(),
                    musicBlock.getTitle(),
                    musicBlock.getBlockOrder(),
                    userMapper.toDTO(musicBlock.getCreator()));
        } else if (block instanceof StatBlock statBlock) {
            return new StatBlockDTO(
                    statBlock.getId(),
                    statBlock.getModuleVersion().getId(),
                    statBlock.getTitle(),
                    statBlock.getBlockOrder(),
                    userMapper.toDTO(statBlock.getCreator()),
                    statBlock.getStatRules(),
                    statBlock.getStatValues());
        } else if (block instanceof IntegratedModuleBlock integratedModuleBlock) {
            return new IntegratedModuleBlockDTO(
                    integratedModuleBlock.getModule().getId(),
                    integratedModuleBlock.getId(),
                    integratedModuleBlock.getModuleVersion().getId(),
                    integratedModuleBlock.getTitle(),
                    integratedModuleBlock.getBlockOrder(),
                    userMapper.toDTO(integratedModuleBlock.getCreator()));
        }
        return null;
    }

    public ModuleVersion toEntity(ModuleVersionDTO moduleVersionDTO) {

        GameSystem gameSystem = gameSystemRepository.findById(moduleVersionDTO.gameSystemId()).orElseThrow(
                () -> new IllegalArgumentException("Invalid game system id: " + moduleVersionDTO.gameSystemId()));

        ModuleVersion moduleVersion = new ModuleVersion();
        moduleVersion.setId(moduleVersionDTO.id());
        moduleVersion.setVersion(moduleVersionDTO.version());
        moduleVersion.setCreator(userRepository.findById(moduleVersionDTO.creator().id()).orElseThrow(
                () -> new IllegalArgumentException("Invalid user id: " + moduleVersionDTO.creator().id())));
        moduleVersion.setCreatedAt(
                moduleVersionDTO.createdAt() != null ? moduleVersionDTO.createdAt() : LocalDateTime.now());
        moduleVersion.setPublished(moduleVersionDTO.published());
        moduleVersion.setGameSystem(gameSystem);
        moduleVersion.setLanguage(moduleVersionDTO.language());
        return moduleVersion;
    }
}
