package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.IntegratedModuleBlockDTO;
import com.ipi.mesi_backend_rpg.dto.ParagraphBlockDTO;
import com.ipi.mesi_backend_rpg.dto.StatBlockDTO;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.IntegratedModuleBlock;
import com.ipi.mesi_backend_rpg.model.ParagraphBlock;
import com.ipi.mesi_backend_rpg.model.StatBlock;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import com.ipi.mesi_backend_rpg.service.ModuleVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlockMapper {

    private final ModuleVersionService moduleVersionService;
    private final ModuleVersionMapper moduleVersionMapper;
    private final ModuleRepository moduleRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final ModuleVersionRepository moduleVersionRepository;

    public BlockDTO toDTO(Block block) {

        if (block instanceof ParagraphBlock paragraphBlock) {
            return new ParagraphBlockDTO(
                    paragraphBlock.getParagraph(),
                    paragraphBlock.getStyle(),
                    paragraphBlock.getId(),
                    paragraphBlock.getModuleVersion().getId(),
                    paragraphBlock.getTitle(),
                    paragraphBlock.getBlockOrder(),
                    userMapper.toDTO(paragraphBlock.getCreator())
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
                    userMapper.toDTO(integratedModuleBlock.getCreator())
            );
        }

        if(block instanceof StatBlock statBlock) {
            return new StatBlockDTO(
                    statBlock.getId(),
                    statBlock.getModuleVersion().getId(),
                    statBlock.getTitle(),
                    statBlock.getBlockOrder(),
                    userMapper.toDTO(statBlock.getCreator()),
                    statBlock.getStatRules(),
                    statBlock.getStatValues()
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
                    userRepository.findById(paragraphBlockDTO.getCreator().id()).orElseThrow(() -> new IllegalArgumentException("Invalid user id: " + paragraphBlockDTO.getCreator().id())),
                    paragraphBlockDTO.getParagraph(),
                    paragraphBlockDTO.getStyle()
            );
        }

        if (blockDTO instanceof IntegratedModuleBlockDTO integratedModuleBlockDTO) {
            return new IntegratedModuleBlock(
                    moduleVersionRepository.findById(integratedModuleBlockDTO.getModuleVersionId()).orElseThrow(() -> new IllegalArgumentException("Invalid module version id: " + integratedModuleBlockDTO.getModuleVersionId())),
                    integratedModuleBlockDTO.getTitle(),
                    integratedModuleBlockDTO.getBlockOrder(),
                    "block",
                    userRepository.findById(integratedModuleBlockDTO.getCreator().id()).orElseThrow(() -> new IllegalArgumentException("Invalid user id: " + integratedModuleBlockDTO.getCreator().id())),
                    moduleRepository.findById(integratedModuleBlockDTO.getModuleId()).orElse(null)
            );
        }

        if(blockDTO instanceof StatBlockDTO statBlockDTO) {
                return new StatBlock(
                        moduleVersionRepository.findById(statBlockDTO.getModuleVersionId()).orElseThrow(() -> new IllegalArgumentException("Invalid module version id: " + statBlockDTO.getModuleVersionId())),
                        statBlockDTO.getTitle(),
                        statBlockDTO.getBlockOrder(),
                        "stat",
                        userRepository.findById(statBlockDTO.getCreator().id()).orElseThrow(() -> new IllegalArgumentException("Invalid user : " + statBlockDTO.getCreator().id())),
                        statBlockDTO.getStatRules(),
                        statBlockDTO.getStatValues()
                );
        }
            //TODO:ajouter type de bloc ici

        throw new IllegalArgumentException("Unknown block type");
    }
}
