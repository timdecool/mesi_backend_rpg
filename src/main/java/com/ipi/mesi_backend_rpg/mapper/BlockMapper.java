package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.*;
import com.ipi.mesi_backend_rpg.model.*;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlockMapper {

    private final ModuleRepository moduleRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final ModuleVersionRepository moduleVersionRepository;
    private final PictureMapper pictureMapper;

    
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

        if (block instanceof StatBlock statBlock) {
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

        if (block instanceof MusicBlock musicBlock) {
            return new MusicBlockDTO(
                    musicBlock.getLabel(),
                    musicBlock.getSrc(),
                    musicBlock.getId(),
                    musicBlock.getModuleVersion().getId(),
                    musicBlock.getTitle(),
                    musicBlock.getBlockOrder(),
                    userMapper.toDTO(musicBlock.getCreator())
            );
        }

        if (block instanceof PictureBlock pictureBlock) {
            return new PictureBlockDTO(
                    pictureBlock.getLabel(),
                    pictureMapper.toDTO(pictureBlock.getPicture()),
                    pictureBlock.getId(),
                    pictureBlock.getModuleVersion().getId(),
                    pictureBlock.getTitle(),
                    pictureBlock.getBlockOrder(),
                    userMapper.toDTO(pictureBlock.getCreator())
            );
        }

        throw new IllegalArgumentException("Unknown block type");
    }

    public Block toEntity(BlockDTO blockDTO) {

        if (blockDTO instanceof ParagraphBlockDTO paragraphBlockDTO) {
            return new ParagraphBlock(
                    moduleVersionRepository.findById(paragraphBlockDTO.getModuleVersionId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid moduleVersion id")),
                    paragraphBlockDTO.getTitle(),
                    paragraphBlockDTO.getBlockOrder(),
                    "paragraph",
                    null,
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
                    null,
                    moduleRepository.findById(integratedModuleBlockDTO.getModuleId()).orElse(null)
            );
        }

        if (blockDTO instanceof StatBlockDTO statBlockDTO) {
            return new StatBlock(
                    moduleVersionRepository.findById(statBlockDTO.getModuleVersionId()).orElseThrow(() -> new IllegalArgumentException("Invalid module version id: " + statBlockDTO.getModuleVersionId())),
                    statBlockDTO.getTitle(),
                    statBlockDTO.getBlockOrder(),
                    "stat",
                    null,
                    statBlockDTO.getStatRules(),
                    statBlockDTO.getStatValues()
            );
        }

        if (blockDTO instanceof MusicBlockDTO musicBlockDTO) {
            return new MusicBlock(
                    musicBlockDTO.getLabel(),
                    musicBlockDTO.getSrc(),
                    moduleVersionRepository.findById(musicBlockDTO.getModuleVersionId()).orElseThrow(() -> new IllegalArgumentException("Invalid module version id: " + musicBlockDTO.getModuleVersionId())),
                    musicBlockDTO.getTitle(),
                    musicBlockDTO.getBlockOrder(),
                    "music",
                    null
            );

        }

        if (blockDTO instanceof PictureBlockDTO pictureBlockDTO) {
            return new PictureBlock(
                    pictureBlockDTO.getLabel(),
                    pictureMapper.toEntity(pictureBlockDTO.getPicture()),
                    moduleVersionRepository.findById(pictureBlockDTO.getModuleVersionId()).orElseThrow(() -> new IllegalArgumentException("Invalid module version id: " + pictureBlockDTO.getModuleVersionId())),
                    pictureBlockDTO.getTitle(),
                    pictureBlockDTO.getBlockOrder(),
                    "picture",
                    null
            );
        }

        //TODO:ajouter type de bloc ici

        throw new IllegalArgumentException("Unknown block type");
    }
}
