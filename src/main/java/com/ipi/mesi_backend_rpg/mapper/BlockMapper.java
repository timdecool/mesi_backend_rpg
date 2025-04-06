package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.dto.ParagraphBlockDTO;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.ParagraphBlock;
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

        if(block instanceof ParagraphBlock paragraphBlock) {
            return new ParagraphBlockDTO(
                    paragraphBlock.getParagraph(),
                    paragraphBlock.getStyle(),
                    paragraphBlock.getId(),
                    paragraphBlock.getModuleVersion().getId(),
                    paragraphBlock.getTitle(),
                    paragraphBlock.getBlockOrder(),
                    paragraphBlock.getCreatedBy()
            );
        }
        //TODO: ajouter type de bloc ici
        throw new IllegalArgumentException("Unknown block type");
    }

    public Block toEntity(BlockDTO blockDTO) {

        if(blockDTO instanceof ParagraphBlockDTO paragraphBlockDTO) {
            return new ParagraphBlock(
                    moduleVersionMapper.toEntity(moduleVersionService.findById(paragraphBlockDTO.getModuleVersionId())),
                    paragraphBlockDTO.getTitle(),
                    paragraphBlockDTO.getBlockOrder(),
                    "paragraph",
                    paragraphBlockDTO.getCreatedBy(),
                    paragraphBlockDTO.getParagraph(),
                    paragraphBlockDTO.getStyle()
            );
        }
        //TODO:ajouter type de bloc ici
        throw new IllegalArgumentException("Unknown block type");
    }

}
