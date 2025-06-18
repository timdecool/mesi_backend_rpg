package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.*;
import com.ipi.mesi_backend_rpg.model.*;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;
import com.ipi.mesi_backend_rpg.repository.PictureRepository; // Import PictureRepository
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class BlockMapper {

    private final ModuleRepository moduleRepository;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final ModuleVersionRepository moduleVersionRepository;
    private final PictureMapper pictureMapper;
    private final PictureRepository pictureRepository; // Ensure this is injected

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
        throw new IllegalArgumentException("Unknown block type: " + block.getClass().getName());
    }

    /**
     * Met à jour une entité Block existante à partir d'un DTO.
     * C'est la méthode à utiliser pour les mises à jour.
     */
    public void updateBlockFromDTO(BlockDTO dto, Block entity) {
        entity.setTitle(dto.getTitle());
        entity.setBlockOrder(dto.getBlockOrder());
        entity.setType(dto.getType());

        if (entity instanceof ParagraphBlock paragraphBlock && dto instanceof ParagraphBlockDTO paragraphDTO) {
            paragraphBlock.setParagraph(paragraphDTO.getParagraph());
            paragraphBlock.setStyle(paragraphDTO.getStyle());
        } else if (entity instanceof MusicBlock musicBlock && dto instanceof MusicBlockDTO musicDTO) {
            musicBlock.setLabel(musicDTO.getLabel());
            musicBlock.setSrc(musicDTO.getSrc());
        } else if (entity instanceof StatBlock statBlock && dto instanceof StatBlockDTO statDTO) {
            statBlock.setStatRules(statDTO.getStatRules());
            statBlock.setStatValues(statDTO.getStatValues());
        } else if (entity instanceof IntegratedModuleBlock integratedBlock && dto instanceof IntegratedModuleBlockDTO integratedDTO) {
            Module module = moduleRepository.findById(integratedDTO.getModuleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module for IntegratedModuleBlock not found"));
            integratedBlock.setModule(module);
        } else if (entity instanceof PictureBlock pictureBlock && dto instanceof PictureBlockDTO pictureDTO) {
            pictureBlock.setLabel(pictureDTO.getLabel());
            if (pictureDTO.getPicture() != null) {
                Picture picture;
                if (pictureDTO.getPicture().id() != null) {
                    // L'image existe, on la récupère et la met à jour
                    picture = pictureRepository.findById(pictureDTO.getPicture().id())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Picture not found with id: " + pictureDTO.getPicture().id()));
                    picture.setTitle(pictureDTO.getPicture().title());
                    picture.setSrc(pictureDTO.getPicture().src());
                    // Note: No explicit save here, as 'picture' is a managed entity and changes will be flushed by the transaction.
                } else {
                    // C'est une nouvelle image
                    picture = pictureMapper.toEntity(pictureDTO.getPicture());
                    picture = pictureRepository.save(picture); // THIS LINE SHOULD REMAIN
                }
                pictureBlock.setPicture(picture);
            } else {
                // Pas d'image fournie, on la supprime de la relation
                pictureBlock.setPicture(null);
            }
        }
    }

    public Block toEntity(BlockDTO blockDTO) {
        if (blockDTO.getId() != null) {
             throw new IllegalArgumentException("toEntity should not be called with a DTO that has an ID.");
        }

        if (blockDTO instanceof ParagraphBlockDTO paragraphBlockDTO) {
            return new ParagraphBlock(
                    moduleVersionRepository.findById(paragraphBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid moduleVersion id")),
                    paragraphBlockDTO.getTitle(),
                    paragraphBlockDTO.getBlockOrder(),
                    "paragraph",
                    userRepository.findById(paragraphBlockDTO.getCreator().id()).orElse(null),
                    paragraphBlockDTO.getParagraph(),
                    paragraphBlockDTO.getStyle());
        }

        if (blockDTO instanceof IntegratedModuleBlockDTO integratedModuleBlockDTO) {
            return new IntegratedModuleBlock(
                    moduleVersionRepository.findById(integratedModuleBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + integratedModuleBlockDTO.getModuleVersionId())),
                    integratedModuleBlockDTO.getTitle(),
                    integratedModuleBlockDTO.getBlockOrder(),
                    "block",
                    userRepository.findById(integratedModuleBlockDTO.getCreator().id()).orElse(null),
                    moduleRepository.findById(integratedModuleBlockDTO.getModuleId()).orElse(null));
        }

        if (blockDTO instanceof StatBlockDTO statBlockDTO) {
            return new StatBlock(
                    moduleVersionRepository.findById(statBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + statBlockDTO.getModuleVersionId())),
                    statBlockDTO.getTitle(),
                    statBlockDTO.getBlockOrder(),
                    "stat",
                    userRepository.findById(statBlockDTO.getCreator().id()).orElse(null),
                    statBlockDTO.getStatRules(),
                    statBlockDTO.getStatValues());
        }

        if (blockDTO instanceof MusicBlockDTO musicBlockDTO) {
            return new MusicBlock(
                    musicBlockDTO.getLabel(),
                    musicBlockDTO.getSrc(),
                    moduleVersionRepository.findById(musicBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + musicBlockDTO.getModuleVersionId())),
                    musicBlockDTO.getTitle(),
                    musicBlockDTO.getBlockOrder(),
                    "music",
                    userRepository.findById(musicBlockDTO.getCreator().id()).orElse(null));

        }

         if (blockDTO instanceof PictureBlockDTO pictureBlockDTO) {
            Picture picture = null;
            if (pictureBlockDTO.getPicture() != null) {
                if (pictureBlockDTO.getPicture().id() != null) {
                    // Si l'image existe déjà, on la récupère depuis la base de données
                    picture = pictureRepository.findById(pictureBlockDTO.getPicture().id())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid picture id: " + pictureBlockDTO.getPicture().id()));
                } else {
                    // Sinon, c'est une nouvelle image, elle doit être persistée
                    picture = pictureMapper.toEntity(pictureBlockDTO.getPicture());
                    picture = pictureRepository.save(picture); // RE-ADD THIS LINE: Persist the new Picture
                }
            }
            
            return new PictureBlock(
                    pictureBlockDTO.getLabel(),
                    picture,
                    moduleVersionRepository.findById(pictureBlockDTO.getModuleVersionId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid module version id: " + pictureBlockDTO.getModuleVersionId())),
                    pictureBlockDTO.getTitle(),
                    pictureBlockDTO.getBlockOrder(),
                    "picture",
                    userRepository.findById(pictureBlockDTO.getCreator().id()).orElse(null));
        }

        throw new IllegalArgumentException("Unknown block type");
    }
}