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
                    userMapper.toDTO(paragraphBlock.getCreator()));
        }
        // TODO: ajouter type de bloc ici

        if (block instanceof IntegratedModuleBlock integratedModuleBlock) {
            return new IntegratedModuleBlockDTO(
                    integratedModuleBlock.getModule().getId(),
                    integratedModuleBlock.getId(),
                    integratedModuleBlock.getModuleVersion().getId(),
                    integratedModuleBlock.getTitle(),
                    integratedModuleBlock.getBlockOrder(),
                    userMapper.toDTO(integratedModuleBlock.getCreator()));
        }

        if (block instanceof StatBlock statBlock) {
            return new StatBlockDTO(
                    statBlock.getId(),
                    statBlock.getModuleVersion().getId(),
                    statBlock.getTitle(),
                    statBlock.getBlockOrder(),
                    userMapper.toDTO(statBlock.getCreator()),
                    statBlock.getStatRules(),
                    statBlock.getStatValues());
        }

        if (block instanceof MusicBlock musicBlock) {
            return new MusicBlockDTO(
                    musicBlock.getLabel(),
                    musicBlock.getSrc(),
                    musicBlock.getId(),
                    musicBlock.getModuleVersion().getId(),
                    musicBlock.getTitle(),
                    musicBlock.getBlockOrder(),
                    userMapper.toDTO(musicBlock.getCreator()));
        }

        if (block instanceof PictureBlock pictureBlock) {
            return new PictureBlockDTO(
                    pictureBlock.getLabel(),
                    pictureMapper.toDTO(pictureBlock.getPicture()),
                    pictureBlock.getId(),
                    pictureBlock.getModuleVersion().getId(),
                    pictureBlock.getTitle(),
                    pictureBlock.getBlockOrder(),
                    userMapper.toDTO(pictureBlock.getCreator()));
        }

        throw new IllegalArgumentException("Unknown block type");
    }

    public Block toEntity(BlockDTO blockDTO) {

        if (blockDTO instanceof ParagraphBlockDTO paragraphBlockDTO) {
            ParagraphBlock paragraphBlock = new ParagraphBlock(
                    moduleVersionRepository.findById(paragraphBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid moduleVersion id")),
                    paragraphBlockDTO.getTitle(),
                    paragraphBlockDTO.getBlockOrder(),
                    "paragraph",
                    userRepository.findById(paragraphBlockDTO.getCreator().id()).orElse(null),
                    paragraphBlockDTO.getParagraph(),
                    paragraphBlockDTO.getStyle());
            
            // Définir l'ID si fourni dans le DTO et qu'il s'agit d'un vrai ID de base de données
            if (paragraphBlockDTO.getId() != null && isValidDatabaseId(paragraphBlockDTO.getId())) {
                paragraphBlock.setId(paragraphBlockDTO.getId());
            }
            return paragraphBlock;
        }

        if (blockDTO instanceof IntegratedModuleBlockDTO integratedModuleBlockDTO) {
            IntegratedModuleBlock integratedModuleBlock = new IntegratedModuleBlock(
                    moduleVersionRepository.findById(integratedModuleBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + integratedModuleBlockDTO.getModuleVersionId())),
                    integratedModuleBlockDTO.getTitle(),
                    integratedModuleBlockDTO.getBlockOrder(),
                    "block",
                    userRepository.findById(integratedModuleBlockDTO.getCreator().id()).orElse(null),
                    moduleRepository.findById(integratedModuleBlockDTO.getModuleId()).orElse(null));
            
            // Définir l'ID si fourni dans le DTO et qu'il s'agit d'un vrai ID de base de données
            if (integratedModuleBlockDTO.getId() != null && isValidDatabaseId(integratedModuleBlockDTO.getId())) {
                integratedModuleBlock.setId(integratedModuleBlockDTO.getId());
            }
            return integratedModuleBlock;
        }

        if (blockDTO instanceof StatBlockDTO statBlockDTO) {
            StatBlock statBlock = new StatBlock(
                    moduleVersionRepository.findById(statBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + statBlockDTO.getModuleVersionId())),
                    statBlockDTO.getTitle(),
                    statBlockDTO.getBlockOrder(),
                    "stat",
                    userRepository.findById(statBlockDTO.getCreator().id()).orElse(null),
                    statBlockDTO.getStatRules(),
                    statBlockDTO.getStatValues());
            
            // Définir l'ID si fourni dans le DTO et qu'il s'agit d'un vrai ID de base de données
            if (statBlockDTO.getId() != null && isValidDatabaseId(statBlockDTO.getId())) {
                statBlock.setId(statBlockDTO.getId());
            }
            return statBlock;
        }

        if (blockDTO instanceof MusicBlockDTO musicBlockDTO) {
            MusicBlock musicBlock = new MusicBlock(
                    musicBlockDTO.getLabel(),
                    musicBlockDTO.getSrc(),
                    moduleVersionRepository.findById(musicBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + musicBlockDTO.getModuleVersionId())),
                    musicBlockDTO.getTitle(),
                    musicBlockDTO.getBlockOrder(),
                    "music",
                    userRepository.findById(musicBlockDTO.getCreator().id()).orElse(null));
            
            // Définir l'ID si fourni dans le DTO et qu'il s'agit d'un vrai ID de base de données
            if (musicBlockDTO.getId() != null && isValidDatabaseId(musicBlockDTO.getId())) {
                musicBlock.setId(musicBlockDTO.getId());
            }
            return musicBlock;
        }

        if (blockDTO instanceof PictureBlockDTO pictureBlockDTO) {
            PictureBlock pictureBlock = new PictureBlock(
                    pictureBlockDTO.getLabel(),
                    pictureMapper.toEntity(pictureBlockDTO.getPicture()),
                    moduleVersionRepository.findById(pictureBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + pictureBlockDTO.getModuleVersionId())),
                    pictureBlockDTO.getTitle(),
                    pictureBlockDTO.getBlockOrder(),
                    "picture",
                    userRepository.findById(pictureBlockDTO.getCreator().id()).orElse(null));
            
            // Définir l'ID si fourni dans le DTO et qu'il s'agit d'un vrai ID de base de données
            if (pictureBlockDTO.getId() != null && isValidDatabaseId(pictureBlockDTO.getId())) {
                pictureBlock.setId(pictureBlockDTO.getId());
            }
            return pictureBlock;
        }

        throw new IllegalArgumentException("Unknown block type");
    }

    /**
     * Vérifie si un ID est un vrai ID de base de données ou un ID temporaire généré côté client
     */
    private boolean isValidDatabaseId(Long id) {
        return id != null && id > 0 && id < 1_000_000_000_000L;
    }
}
