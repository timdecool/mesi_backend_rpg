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

    @Transactional // This transaction will manage all changes
    public void synchronizeBlocks(Long targetModuleVersionId, List<BlockDTO> incomingBlockDTOs) {
        if (targetModuleVersionId == null) {
            throw new IllegalArgumentException("Target ModuleVersionId cannot be null.");
        }

        ModuleVersion moduleVersion = moduleVersionRepository.findById(targetModuleVersionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Target ModuleVersion with id " + targetModuleVersionId + " not found."));

        List<BlockDTO> dtosToProcess = (incomingBlockDTOs == null) ? new ArrayList<>() : incomingBlockDTOs;

        // Create a mutable copy of the existing blocks from the ModuleVersion entity
        // IMPORTANT: Ensure moduleVersion.getBlocks() is initialized before calling stream()
        List<Block> currentBlocksInDb = new ArrayList<>(moduleVersion.getBlocks());

        // Map existing blocks by ID for efficient lookup during updates
        Map<Long, Block> currentDbBlocksMap = currentBlocksInDb.stream()
                .filter(Objects::nonNull) // Filter out any null elements if they exist (shouldn't if data is clean)
                .collect(Collectors.toMap(Block::getId, Function.identity()));

        // Prepare a list to hold the blocks for the updated moduleVersion.blocks collection
        List<Block> newBlocksCollection = new ArrayList<>();

        for (BlockDTO dto : dtosToProcess) {
            if (dto.getId() != null) {
                // This DTO represents an existing block - attempt to update
                Block existingBlock = currentDbBlocksMap.get(dto.getId());
                if (existingBlock != null) {
                    // Update the existing block entity
                    blockMapper.updateBlockFromDTO(dto, existingBlock);
                    existingBlock.setUpdatedAt(LocalDate.now());
                    newBlocksCollection.add(existingBlock); // Add the updated entity to the new collection
                } else {
                    // DTO with an ID that doesn't exist in DB for this version (error)
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Block with ID " + dto.getId() + " not found in DB for module version "
                                    + targetModuleVersionId + " during synchronization.");
                }
            } else {
                // This DTO represents a new block - create and add
                Block newBlock = blockMapper.toEntity(dto); // Creates a transient new Block
                newBlock.setModuleVersion(moduleVersion); // Link to the current module version
                newBlock.setCreator(userService.getAuthenticatedUser()); // Set creator
                newBlock.setCreatedAt(LocalDate.now());
                newBlock.setUpdatedAt(LocalDate.now());
                newBlocksCollection.add(newBlock); // Add the new transient entity to the new collection
            }
        }

        // Now, update the actual collection on the managed ModuleVersion entity.
        // Hibernate's orphanRemoval=true on ModuleVersion.blocks will handle deletions
        // for blocks present in currentBlocksInDb but not in newBlocksCollection.
        moduleVersion.getBlocks().clear(); // Clear existing collection
        moduleVersion.getBlocks().addAll(newBlocksCollection); // Add all updated/new blocks

        // No need to explicitly save blocksToSave or call deleteBlock here.
        // The ModuleVersion entity is already managed. When the transaction commits,
        // Hibernate will detect changes to its 'blocks' collection and apply them.
        // This includes deleting "orphaned" blocks.
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