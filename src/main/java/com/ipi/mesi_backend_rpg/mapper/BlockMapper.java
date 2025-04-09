package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.IntegratedModuleBlockDTO;
import com.ipi.mesi_backend_rpg.dto.ParagraphBlockDTO;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.IntegratedModuleBlock;
import com.ipi.mesi_backend_rpg.model.ParagraphBlock;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.service.ModuleVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlockMapper {

    private final ModuleVersionService moduleVersionService;
    private final ModuleVersionMapper moduleVersionMapper;
    private final ModuleRepository moduleRepository;
    
    public BlockDTO toDTO(Block block) {

        if (block instanceof ParagraphBlock paragraphBlock) {
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

        if (block instanceof IntegratedModuleBlock integratedModuleBlock) {
            return new IntegratedModuleBlockDTO(
                    integratedModuleBlock.getModule().getId(),
                    integratedModuleBlock.getId(),
                    integratedModuleBlock.getModuleVersion().getId(),
                    integratedModuleBlock.getTitle(),
                    integratedModuleBlock.getBlockOrder(),
                    integratedModuleBlock.getCreatedBy()
            );
        }

        throw new IllegalArgumentException("Unknown block type");
    }

    public Block toEntity(BlockDTO blockDTO) {

        if (blockDTO instanceof ParagraphBlockDTO paragraphBlockDTO) {
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

        if (blockDTO instanceof IntegratedModuleBlockDTO integratedModuleBlockDTO) {
            return new IntegratedModuleBlock(
                    moduleVersionMapper.toEntity(moduleVersionService.findById(integratedModuleBlockDTO.getModuleVersionId())),
                    integratedModuleBlockDTO.getTitle(),
                    integratedModuleBlockDTO.getBlockOrder(),
                    "block",
                    integratedModuleBlockDTO.getCreatedBy(),
                    moduleRepository.findById(integratedModuleBlockDTO.getModuleId()).orElse(null)
            );
            //TODO:ajouter type de bloc ici
        }

        throw new IllegalArgumentException("Unknown block type");
    }
}
