package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.service.ModuleVersionService;
import org.springframework.stereotype.Service;

@Service
public class BlockMapper {

    private final ModuleVersionService moduleVersionService;
    private final ModuleVersionMapper moduleVersionMapper;

    public BlockMapper(ModuleVersionService moduleVersionService, ModuleVersionMapper moduleVersionMapper) {
        this.moduleVersionService = moduleVersionService;
        this.moduleVersionMapper = moduleVersionMapper;
    }

    public BlockDTO toDTO(Block block) {
        return new BlockDTO(
                block.getId(),
                block.getModuleVersion().getId(),
                block.getTitle(),
                block.getType(),
                block.getBlockOrder(),
                block.getCreatedBy()
        );
    }

    public Block toEntity(BlockDTO blockDTO) {

        ModuleVersionDTO moduleVersion = moduleVersionService.findById(blockDTO.moduleVersionId());

        Block block = new Block();
        block.setId(blockDTO.id());
        block.setModuleVersion(moduleVersionMapper.toEntity(moduleVersion));
        block.setTitle(blockDTO.title());
        block.setType(blockDTO.type());
        block.setBlockOrder(blockDTO.blockOrder());
        block.setCreatedBy(blockDTO.createdBy());
        return block;
    }

}
