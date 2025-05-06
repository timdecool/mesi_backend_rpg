package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.mapper.BlockMapper;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.BlockRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final BlockMapper blockMapper;
    private final ModuleVersionRepository moduleVersionRepository;

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

    public BlockDTO createBlock(BlockDTO blockDTO) {
        // Vérifier que la version du module existe
        moduleVersionRepository.findById(blockDTO.getModuleVersionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module version not found"));
        
        Block block = blockMapper.toEntity(blockDTO);
        block.setCreatedAt(LocalDate.now());
        block.setUpdatedAt(LocalDate.now());
        blockRepository.save(block);
        return blockMapper.toDTO(block);
    }

    public BlockDTO updateBlock(Long id, BlockDTO blockDTO) {
        Block existingBlock = blockRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Block not found"));
        
        if (!existingBlock.getId().equals(blockDTO.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and body do not match");
        }
        
        // Vérifier que la version du module existe
        moduleVersionRepository.findById(blockDTO.getModuleVersionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module version not found"));
        
        Block updatedBlock = blockMapper.toEntity(blockDTO);
        updatedBlock.setId(existingBlock.getId());
        updatedBlock.setCreatedAt(existingBlock.getCreatedAt());
        updatedBlock.setUpdatedAt(LocalDate.now());
        
        blockRepository.save(updatedBlock);
        return blockMapper.toDTO(updatedBlock);
    }

    public void deleteBlock(Long id) {
        Block block = blockRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Block not found"));
        
        blockRepository.delete(block);
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
