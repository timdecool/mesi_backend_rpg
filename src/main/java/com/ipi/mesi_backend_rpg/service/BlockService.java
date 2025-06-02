package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.IntegratedModuleBlockDTO;
import com.ipi.mesi_backend_rpg.dto.MusicBlockDTO;
import com.ipi.mesi_backend_rpg.dto.ParagraphBlockDTO;
import com.ipi.mesi_backend_rpg.dto.PictureBlockDTO;
import com.ipi.mesi_backend_rpg.dto.StatBlockDTO;
import com.ipi.mesi_backend_rpg.mapper.BlockMapper;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.BlockRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final BlockMapper blockMapper;
    private final ModuleVersionRepository moduleVersionRepository;
    private final UserRepository userRepository;

    public List<BlockDTO> getAllBlocksByModuleVersionId(Long moduleVersionId) {
        ModuleVersion moduleVersion = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module version not found"));

        List<Block> blocks = blockRepository.findAllByModuleVersion(moduleVersion);
        return blocks.stream().map(blockMapper::toDTO).collect(Collectors.toList());
    }

    public List<BlockDTO> getAllBlocks(ModuleVersion moduleVersion) {
        List<Block> blocks = blockRepository.findAllByModuleVersion(moduleVersion);
        return blocks.stream().map(block -> blockMapper.toDTO(block)).toList();
    }

    public BlockDTO createBlock(BlockDTO blockDTO) {
        // Vérifier que la version du module existe
        moduleVersionRepository.findById(blockDTO.getModuleVersionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module version not found"));

        Block block = blockMapper.toEntity(blockDTO);
        block.setCreatedAt(LocalDate.now());
        block.setUpdatedAt(LocalDate.now());
        blockRepository.save(block);
        return blockMapper.toDTO(block);
    }

    public BlockDTO updateBlock(Long id, BlockDTO blockDTO) {
        Block existingBlock = blockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Block not found"));

        if (!existingBlock.getId().equals(blockDTO.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and body do not match");
        }

        if (blockDTO.getEntityVersion() != null &&
                !existingBlock.getVersion().equals(blockDTO.getEntityVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le bloc a été modifié par un autre utilisateur. " +
                            "Veuillez recharger la page et réessayer.");
        }

        try {
            moduleVersionRepository.findById(blockDTO.getModuleVersionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module version not found"));

            Block updatedBlock = blockMapper.toEntity(blockDTO);
            updatedBlock.setId(existingBlock.getId());
            updatedBlock.setCreatedAt(existingBlock.getCreatedAt());
            updatedBlock.setUpdatedAt(LocalDate.now());

            blockRepository.save(updatedBlock);
            return blockMapper.toDTO(updatedBlock);

        } catch (OptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Conflit de modification détecté. Le bloc a été modifié par un autre utilisateur " +
                            "pendant votre édition. Veuillez recharger et réessayer.");
        }
    }

    public void deleteBlock(Long id) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Block not found"));

        blockRepository.delete(block);
    }

    public void synchronizeBlocks(Long targetModuleVersionId, List<BlockDTO> incomingBlockDTOs) {
        if (targetModuleVersionId == null) {
            throw new IllegalArgumentException("La cible ModuleVersionId ne peut pas être null.");
        }
        // Valider que la ModuleVersion cible existe
        ModuleVersion moduleVersion = moduleVersionRepository.findById(targetModuleVersionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La cible ModuleVersion avec l'id " + targetModuleVersionId + " est introuvable."));

        List<BlockDTO> dtosToProcess = (incomingBlockDTOs == null) ? new ArrayList<>() : incomingBlockDTOs;

        // 1. Récupérer les blocs actuels de cette version de module
        Map<Long, Block> currentDbBlocksMap = blockRepository.findAllByModuleVersion(moduleVersion).stream()
                .collect(Collectors.toMap(Block::getId, Function.identity()));

        // 2. Ensemble des IDs des DTOs entrants qui ont un ID non nul (pour identifier
        // les mises à jour et les suppressions)
        Set<Long> incomingDtoNonNullIds = dtosToProcess.stream()
                .map(BlockDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Traiter les créations et les mises à jour
        for (BlockDTO dto : dtosToProcess) {
            if (dto.getModuleVersionId() != null && !dto.getModuleVersionId().equals(targetModuleVersionId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "BlockDTO (id: " + dto.getId() + ", title: " + dto.getTitle() + ") avait moduleVersionId "
                                + dto.getModuleVersionId() +
                                "qui rentre en conflit avec le module version " + targetModuleVersionId);
            }

            com.ipi.mesi_backend_rpg.dto.UserDTO creatorDto = dto.getCreator();
            if (creatorDto == null || creatorDto.id() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Le créateur manque pour le block: " + dto.getTitle());
            }
            // S'assurer que le créateur existe
            userRepository.findById(creatorDto.id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Le créateur du module avec l'id" + creatorDto.id() + " est introuvable."));

            if (dto.getId() != null) { // Le DTO suggère une MISE À JOUR
                Block existingBlock = currentDbBlocksMap.get(dto.getId());
                if (existingBlock != null) {
                    // S'assurer que le bloc existant appartient bien à la version cible (devrait
                    // toujours être le cas si currentDbBlocksMap est bien peuplé)
                    if (!existingBlock.getModuleVersion().getId().equals(targetModuleVersionId)) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, // Incohérence de données
                                "Block avec l'ID " + dto.getId()
                                        + " trouvé mais n'appartient pas a cette version de module"
                                        + targetModuleVersionId + ".");
                    }
                    updateBlock(dto.getId(), dto);
                } else {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Block avec l'ID " + dto.getId() + "n'a pas été trouver pour la version de ce module"
                                    + targetModuleVersionId + ".");
                }
            } else { // Le DTO suggère une CRÉATION (id est null)
                // Préparer un DTO pour la création, en s'assurant que moduleVersionId et
                // creator sont corrects
                BlockDTO dtoForCreate;
                if (dto instanceof ParagraphBlockDTO pDto) {
                    dtoForCreate = new ParagraphBlockDTO(pDto.getParagraph(), pDto.getStyle(), null,
                            targetModuleVersionId, pDto.getTitle(), pDto.getBlockOrder(), creatorDto);
                } else if (dto instanceof MusicBlockDTO mDto) {
                    dtoForCreate = new MusicBlockDTO(mDto.getLabel(), mDto.getSrc(), null, targetModuleVersionId,
                            mDto.getTitle(), mDto.getBlockOrder(), creatorDto);
                } else if (dto instanceof StatBlockDTO sDto) {
                    dtoForCreate = new StatBlockDTO(null, targetModuleVersionId, sDto.getTitle(), sDto.getBlockOrder(),
                            creatorDto, sDto.getStatRules(), sDto.getStatValues());
                } else if (dto instanceof IntegratedModuleBlockDTO iDto) {
                    dtoForCreate = new IntegratedModuleBlockDTO(iDto.getModuleId(), null, targetModuleVersionId,
                            iDto.getTitle(), iDto.getBlockOrder(), creatorDto);
                } else if (dto instanceof PictureBlockDTO picDto) {
                    dtoForCreate = new PictureBlockDTO(picDto.getLabel(), picDto.getPicture(), null,
                            targetModuleVersionId, picDto.getTitle(), picDto.getBlockOrder(), creatorDto);
                }
                // Ajoutez d'autres types de blocs ici
                else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Le type de Block est inconnue pour la création de: " + dto.getClass().getName());
                }
                createBlock(dtoForCreate);
            }
        }

        // 4. Traiter les SUPPRESSIONS
        Set<Long> dbIdsToDelete = currentDbBlocksMap.keySet().stream()
                .filter(dbId -> !incomingDtoNonNullIds.contains(dbId))
                .collect(Collectors.toSet());

        for (Long idToDelete : dbIdsToDelete) {
            deleteBlock(idToDelete);
        }
    }

    public void createOrUpdateBlocks(List<BlockDTO> blockDTOs) {
        for (BlockDTO blockDTO : blockDTOs) {
            if (blockDTO.getId() != null) {
                updateBlock(blockDTO.getId(), blockDTO);
            } else {
                createBlock(blockDTO);
            }
        }
    }
}
