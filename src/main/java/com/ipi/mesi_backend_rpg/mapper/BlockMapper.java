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
        BlockDTO dto;

        if (block instanceof ParagraphBlock paragraphBlock) {
            dto = new ParagraphBlockDTO(
                    paragraphBlock.getParagraph(),
                    paragraphBlock.getStyle(),
                    paragraphBlock.getId(),
                    paragraphBlock.getModuleVersion().getId(),
                    paragraphBlock.getTitle(),
                    paragraphBlock.getBlockOrder(),
                    userMapper.toDTO(paragraphBlock.getCreator()));
        } else if (block instanceof IntegratedModuleBlock integratedModuleBlock) {
            dto = new IntegratedModuleBlockDTO(
                    integratedModuleBlock.getModule().getId(),
                    integratedModuleBlock.getId(),
                    integratedModuleBlock.getModuleVersion().getId(),
                    integratedModuleBlock.getTitle(),
                    integratedModuleBlock.getBlockOrder(),
                    userMapper.toDTO(integratedModuleBlock.getCreator()));
        } else if (block instanceof StatBlock statBlock) {
            dto = new StatBlockDTO(
                    statBlock.getId(),
                    statBlock.getModuleVersion().getId(),
                    statBlock.getTitle(),
                    statBlock.getBlockOrder(),
                    userMapper.toDTO(statBlock.getCreator()),
                    statBlock.getStatRules(),
                    statBlock.getStatValues());
        } else if (block instanceof MusicBlock musicBlock) {
            dto = new MusicBlockDTO(
                    musicBlock.getLabel(),
                    musicBlock.getSrc(),
                    musicBlock.getId(),
                    musicBlock.getModuleVersion().getId(),
                    musicBlock.getTitle(),
                    musicBlock.getBlockOrder(),
                    userMapper.toDTO(musicBlock.getCreator()));
        } else if (block instanceof PictureBlock pictureBlock) {
            dto = new PictureBlockDTO(
                    pictureBlock.getLabel(),
                    pictureMapper.toDTO(pictureBlock.getPicture()),
                    pictureBlock.getId(),
                    pictureBlock.getModuleVersion().getId(),
                    pictureBlock.getTitle(),
                    pictureBlock.getBlockOrder(),
                    userMapper.toDTO(pictureBlock.getCreator()));
        } else {
            throw new IllegalArgumentException("Unknown block type");
        }

        dto.setEntityVersion(block.getVersion());
        dto.setLastModified(block.getLastModified());
        return dto;
    }

    public Block toEntity(BlockDTO blockDTO) {
        Block block;

        if (blockDTO instanceof ParagraphBlockDTO paragraphBlockDTO) {
            block = new ParagraphBlock(
                    moduleVersionRepository.findById(paragraphBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid moduleVersion id")),
                    paragraphBlockDTO.getTitle(),
                    paragraphBlockDTO.getBlockOrder(),
                    "paragraph",
                    userRepository.findById(paragraphBlockDTO.getCreator().id())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid user id")),
                    paragraphBlockDTO.getParagraph(),
                    paragraphBlockDTO.getStyle());
        } else if (blockDTO instanceof IntegratedModuleBlockDTO integratedModuleBlockDTO) {
            block = new IntegratedModuleBlock(
                    moduleVersionRepository.findById(integratedModuleBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + integratedModuleBlockDTO.getModuleVersionId())),
                    integratedModuleBlockDTO.getTitle(),
                    integratedModuleBlockDTO.getBlockOrder(),
                    "block",
                    userRepository.findById(integratedModuleBlockDTO.getCreator().id())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid user id: " + integratedModuleBlockDTO.getCreator().id())),
                    moduleRepository.findById(integratedModuleBlockDTO.getModuleId()).orElse(null));
        } else if (blockDTO instanceof StatBlockDTO statBlockDTO) {
            block = new StatBlock(
                    moduleVersionRepository.findById(statBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + statBlockDTO.getModuleVersionId())),
                    statBlockDTO.getTitle(),
                    statBlockDTO.getBlockOrder(),
                    "stat",
                    userRepository.findById(statBlockDTO.getCreator().id()).orElseThrow(
                            () -> new IllegalArgumentException("Invalid user : " + statBlockDTO.getCreator().id())),
                    statBlockDTO.getStatRules(),
                    statBlockDTO.getStatValues());
        } else if (blockDTO instanceof MusicBlockDTO musicBlockDTO) {
            block = new MusicBlock(
                    musicBlockDTO.getLabel(),
                    musicBlockDTO.getSrc(),
                    moduleVersionRepository.findById(musicBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + musicBlockDTO.getModuleVersionId())),
                    musicBlockDTO.getTitle(),
                    musicBlockDTO.getBlockOrder(),
                    "music",
                    userRepository.findById(musicBlockDTO.getCreator().id()).orElseThrow(
                            () -> new IllegalArgumentException("Invalid user : " + musicBlockDTO.getCreator().id())));
        } else if (blockDTO instanceof PictureBlockDTO pictureBlockDTO) {
            block = new PictureBlock(
                    pictureBlockDTO.getLabel(),
                    pictureMapper.toEntity(pictureBlockDTO.getPicture()),
                    moduleVersionRepository.findById(pictureBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + pictureBlockDTO.getModuleVersionId())),
                    pictureBlockDTO.getTitle(),
                    pictureBlockDTO.getBlockOrder(),
                    "picture",
                    userRepository.findById(pictureBlockDTO.getCreator().id()).orElseThrow(
                            () -> new IllegalArgumentException("Invalid user: " + pictureBlockDTO.getCreator().id())));
        } else {
            throw new IllegalArgumentException("Unknown block type");
        }

        // ✅ Mapper la version JPA si présente dans le DTO
        if (blockDTO.getEntityVersion() != null) {
            block.setVersion(blockDTO.getEntityVersion());
        }

        return block;
    }
}
