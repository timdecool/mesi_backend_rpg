package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.mapper.BlockMapper;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final BlockMapper blockMapper;
    
    public List<BlockDTO> getAllBlocks(ModuleVersion moduleVersion) {
        List<Block> blocks = blockRepository.findAllByModuleVersion(moduleVersion);
        return blocks.stream().map(block -> blockMapper.toDTO(block)).toList();
    }

    public BlockDTO createBlock(BlockDTO blockDTO) {
        Block block = blockMapper.toEntity(blockDTO);
        block.setCreatedAt(LocalDate.now());
        block.setUpdatedAt(LocalDate.now());
        blockRepository.save(block);
        return blockMapper.toDTO(block);
    }

    public BlockDTO updateBlock(Long id, BlockDTO blockDTO) {

        Block existingBlock = blockRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!existingBlock.getId().equals(blockDTO.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and body do not match");
        }

        Block updatedBlock = blockMapper.toEntity(blockDTO);
        updatedBlock.setId(existingBlock.getId());
        updatedBlock.setUpdatedAt(LocalDate.now());
        blockRepository.save(updatedBlock);
        return blockMapper.toDTO(updatedBlock);
    }

    public BlockDTO deleteBlock(Long id) {
        Block block = blockRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        blockRepository.delete(block);
        return blockMapper.toDTO(block);
    }
}
