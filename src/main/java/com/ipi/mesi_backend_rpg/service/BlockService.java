package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.mapper.BlockMapper;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.BlockRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final BlockMapper blockMapper;
    private final ModuleVersionRepository moduleVersionRepository;
    private final UserService userService;

    public List<BlockDTO> getAllBlocksByModuleVersionId(Long moduleVersionId) {
        ModuleVersion moduleVersion = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module version not found"));

        List<Block> blocks = blockRepository.findAllByModuleVersion(moduleVersion);
        return blocks.stream().map(blockMapper::toDTO).collect(Collectors.toList());
    }

    public List<BlockDTO> getAllBlocks(ModuleVersion moduleVersion) {
        List<Block> blocks = blockRepository.findAllByModuleVersion(moduleVersion);
        return blocks.stream().map(block -> blockMapper.toDTO(block)).toList();
    }

    @Transactional
    public BlockDTO createBlock(BlockDTO blockDTO) {
        // Vérifier que la version du module existe
        moduleVersionRepository.findById(blockDTO.getModuleVersionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module version not found"));

        // La méthode toEntity ne doit être appelée que pour la création.
        // Assurons-nous que le DTO n'a pas d'ID.
        if (blockDTO.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create a block with an existing ID.");
        }
        
        Block block = blockMapper.toEntity(blockDTO);
        block.setCreator(userService.getAuthenticatedUser());
        block.setCreatedAt(LocalDate.now());
        block.setUpdatedAt(LocalDate.now());

        Block savedBlock = blockRepository.save(block);
        return blockMapper.toDTO(savedBlock);
    }

    @Transactional
    public BlockDTO updateBlock(Long id, BlockDTO blockDTO) {
        Block existingBlock = blockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Block not found"));

        if (blockDTO.getId() != null && !blockDTO.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and body do not match");
        }

        // On met à jour l'entité existante (managed) avec les données du DTO
        blockMapper.updateBlockFromDTO(blockDTO, existingBlock);
        existingBlock.setUpdatedAt(LocalDate.now());

        // On sauvegarde l'entité mise à jour
        Block savedBlock = blockRepository.save(existingBlock);
        return blockMapper.toDTO(savedBlock);
    }

    @Transactional
    public void deleteBlock(Long id) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Block not found"));
        blockRepository.delete(block);
    }

    @Transactional
    public void synchronizeBlocks(Long targetModuleVersionId, List<BlockDTO> incomingBlockDTOs) {
        if (targetModuleVersionId == null) {
            throw new IllegalArgumentException("La cible ModuleVersionId ne peut pas être null.");
        }
        ModuleVersion moduleVersion = moduleVersionRepository.findById(targetModuleVersionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La cible ModuleVersion avec l'id " + targetModuleVersionId + " est introuvable."));

        List<BlockDTO> dtosToProcess = (incomingBlockDTOs == null) ? new ArrayList<>() : incomingBlockDTOs;

        Map<Long, Block> currentDbBlocksMap = blockRepository.findAllByModuleVersion(moduleVersion).stream()
                .collect(Collectors.toMap(Block::getId, Function.identity()));

        Set<Long> incomingDtoNonNullIds = dtosToProcess.stream()
                .map(BlockDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (BlockDTO dto : dtosToProcess) {
            if (dto.getId() != null) { 
                if (currentDbBlocksMap.containsKey(dto.getId())) {
                    updateBlock(dto.getId(), dto);
                } else {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Block avec l'ID " + dto.getId() + " n'a pas été trouvé pour la version de ce module "
                                    + targetModuleVersionId + ".");
                }
            } else {
                createBlock(dto);
            }
        }

        Set<Long> dbIdsToDelete = currentDbBlocksMap.keySet().stream()
                .filter(dbId -> !incomingDtoNonNullIds.contains(dbId))
                .collect(Collectors.toSet());

        for (Long idToDelete : dbIdsToDelete) {
            deleteBlock(idToDelete);
        }
    }

    public void createOrUpdateBlocks(List<BlockDTO> blockDTOs) {
        for (BlockDTO blockDTO : blockDTOs) {
            if (blockDTO.getId() != null) {
                updateBlock(blockDTO.getId(), blockDTO);
            } else {
                createBlock(blockDTO);
            }
        }
    }
}
