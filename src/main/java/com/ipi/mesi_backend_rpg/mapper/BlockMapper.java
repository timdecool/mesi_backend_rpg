package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.model.Block;
import org.springframework.stereotype.Service;

@Service
public class BlockMapper {

    public BlockDTO toDTO(Block block) {
        return new BlockDTO(
                block.getId(),
                block.getModuleVersion(),
                block.getTitle(),
                block.getType(),
                block.getBlockOrder(),
                block.getCreatedBy()
        );
    }

    public Block toEntity(BlockDTO blockDTO) {
        Block block = new Block();
        block.setId(blockDTO.id());
        block.setModuleVersion(blockDTO.moduleVersion());
        block.setTitle(blockDTO.title());
        block.setType(blockDTO.type());
        block.setBlockOrder(blockDTO.blockOrder());
        block.setCreatedBy(blockDTO.createdBy());
        return block;
    }

}
