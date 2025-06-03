package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.IntegratedModuleBlockDTO;
import com.ipi.mesi_backend_rpg.dto.MusicBlockDTO;
import com.ipi.mesi_backend_rpg.dto.ParagraphBlockDTO;
import com.ipi.mesi_backend_rpg.dto.PictureBlockDTO;
import com.ipi.mesi_backend_rpg.dto.StatBlockDTO;
import com.ipi.mesi_backend_rpg.mapper.BlockMapper;
import com.ipi.mesi_backend_rpg.repository.BlockRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Ajout de l'import pour Slf4j

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

import com.ipi.mesi_backend_rpg.model.*;

@Service
@RequiredArgsConstructor
@Slf4j // Ajout de l'annotation pour le logging
public class BlockService {

    private final BlockRepository blockRepository;
    private final BlockMapper blockMapper;
    private final ModuleVersionRepository moduleVersionRepository;
    private final UserRepository userRepository;

    public List<BlockDTO> getAllBlocksByModuleVersionId(Long moduleVersionId) {
        ModuleVersion moduleVersion = moduleVersionRepository.findById(moduleVersionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module version not found with id: " + moduleVersionId));

        List<Block> blocks = blockRepository.findAllByModuleVersion(moduleVersion);
        return blocks.stream().map(blockMapper::toDTO).collect(Collectors.toList());
    }

    public List<BlockDTO> getAllBlocks(ModuleVersion moduleVersion) {
        if (moduleVersion == null) {
            throw new IllegalArgumentException("ModuleVersion cannot be null");
        }
        List<Block> blocks = blockRepository.findAllByModuleVersion(moduleVersion);
        return blocks.stream().map(blockMapper::toDTO).toList();
    }

    public BlockDTO createBlock(BlockDTO blockDTO) {
        if (blockDTO.getModuleVersionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ModuleVersionId is required to create a block.");
        }
        moduleVersionRepository.findById(blockDTO.getModuleVersionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module version not found with id: " + blockDTO.getModuleVersionId()));

        if (blockDTO.getCreator() == null || blockDTO.getCreator().id() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator information is required to create a block.");
        }
        userRepository.findById(blockDTO.getCreator().id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator user not found with id: " + blockDTO.getCreator().id()));


        Block block = blockMapper.toEntity(blockDTO);
        block.setCreatedAt(LocalDate.now());
        block.setUpdatedAt(LocalDate.now());
        // La version (entityVersion) sera initialisée par JPA si c'est une nouvelle entité
        blockRepository.save(block);
        return blockMapper.toDTO(block);
    }

    public BlockDTO updateBlock(Long id, BlockDTO blockDTO) {
        Block existingBlock = blockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Block not found with id: " + id));

        if (blockDTO.getId() != null && !existingBlock.getId().equals(blockDTO.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and DTO do not match for update. Path ID: " + id + ", DTO ID: " + blockDTO.getId());
        }

        // Gestion de la concurrence optimiste
        if (blockDTO.getEntityVersion() != null &&
                existingBlock.getVersion() != null &&
                !existingBlock.getVersion().equals(blockDTO.getEntityVersion())) {
            log.warn("Optimistic locking conflict for Block ID {}. DB version: {}, DTO version: {}", id, existingBlock.getVersion(), blockDTO.getEntityVersion());
            throw new OptimisticLockingFailureException(
                    "Le bloc avec id " + id + " a été modifié par un autre utilisateur. Version actuelle: "
                            + existingBlock.getVersion() + ", votre version: " + blockDTO.getEntityVersion());
        }

        try {
            if (blockDTO.getModuleVersionId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "BlockDTO must have a moduleVersionId for update.");
            }
            // Vérifier que la version du module du DTO existe
            moduleVersionRepository.findById(blockDTO.getModuleVersionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Module version specified in BlockDTO not found: " + blockDTO.getModuleVersionId()));
            
            // Vérifier que le créateur du DTO existe
            if (blockDTO.getCreator() == null || blockDTO.getCreator().id() == null) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator information is required for the block.");
            }
            userRepository.findById(blockDTO.getCreator().id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator user not found with id: " + blockDTO.getCreator().id()));


            Block blockToUpdate = blockMapper.toEntity(blockDTO);
            blockToUpdate.setId(existingBlock.getId()); // Assurer que c'est une mise à jour de l'entité existante
            blockToUpdate.setCreatedAt(existingBlock.getCreatedAt()); // Préserver la date de création originale
            blockToUpdate.setUpdatedAt(LocalDate.now()); // Mettre à jour la date de modification
            
            // Appliquer la version de l'entité existante pour que JPA puisse détecter les conflits
            // Cette valeur est déjà vérifiée au début de la méthode.
            blockToUpdate.setVersion(existingBlock.getVersion());

            Block savedBlock = blockRepository.save(blockToUpdate);
            return blockMapper.toDTO(savedBlock);

        } catch (OptimisticLockingFailureException e) {
            log.error("OptimisticLockingFailureException during Block update for ID {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Conflit de modification détecté sur le bloc ID " + id + ". " + e.getMessage(), e);
        }
    }

    public void deleteBlock(Long id) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Block not found with id: " + id + " for deletion."));

        blockRepository.delete(block);
    }

    public void synchronizeBlocks(Long targetModuleVersionId, List<BlockDTO> incomingBlockDTOs) {
        if (targetModuleVersionId == null) {
            throw new IllegalArgumentException("Target ModuleVersionId cannot be null.");
        }
        ModuleVersion moduleVersion = moduleVersionRepository.findById(targetModuleVersionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Target ModuleVersion with id " + targetModuleVersionId + " not found."));

        List<BlockDTO> dtosToProcess = (incomingBlockDTOs == null) ? new ArrayList<>() : incomingBlockDTOs;

        Map<Long, Block> currentDbBlocksMap = blockRepository.findAllByModuleVersion(moduleVersion).stream()
                .collect(Collectors.toMap(Block::getId, Function.identity()));

        // IDs des DTOs entrants qui ont un ID (donc pour mise à jour potentielle)
        Set<Long> incomingDtoNonNullIds = dtosToProcess.stream()
                .map(BlockDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (BlockDTO dto : dtosToProcess) {
            // Valider et préparer le DTO
            if (dto.getCreator() == null || dto.getCreator().id() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator information is missing for block: " + dto.getTitle());
            }
            userRepository.findById(dto.getCreator().id())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator user not found with id: " + dto.getCreator().id()));

            // Assurer que le moduleVersionId du DTO est cohérent avec targetModuleVersionId
            // Si le DTO a un moduleVersionId, il DOIT correspondre à targetModuleVersionId.
            // S'il n'en a pas (pour une création), il sera implicitement lié à targetModuleVersionId.
            if (dto.getModuleVersionId() != null && !dto.getModuleVersionId().equals(targetModuleVersionId)) {
                 throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "BlockDTO (id: " + (dto.getId() != null ? dto.getId() : "Nouveau") + ", title: "
                                + dto.getTitle() + ") has a moduleVersionId ("
                                + dto.getModuleVersionId() +
                                ") that conflicts with the target module version (" + targetModuleVersionId
                                + ")");
            }


            if (dto.getId() != null) { // Le DTO suggère une MISE À JOUR
                // Vérifier si le bloc existe toujours en base avant de tenter la mise à jour.
                // Utiliser blockRepository.existsById pour une vérification légère si le bloc est déjà dans currentDbBlocksMap.
                if (currentDbBlocksMap.containsKey(dto.getId()) || blockRepository.existsById(dto.getId())) {
                    // S'assurer que le moduleVersionId du DTO est celui de la cible pour l'update.
                    // Si le DTO vient d'une source externe, il faut s'assurer qu'il est correctement ciblé.
                    // Pour la mise à jour, le DTO doit refléter l'état désiré pour CE moduleVersion.
                    BlockDTO dtoForUpdate = prepareDtoForOperation(dto, targetModuleVersionId);
                    try {
                        updateBlock(dto.getId(), dtoForUpdate);
                    } catch (OptimisticLockingFailureException olfe) {
                        log.warn("Optimistic lock during synchronizeBlocks for block ID {}: {}. Propagating.", dto.getId(), olfe.getMessage());
                        throw olfe; // Laisser le MergeService gérer cela plus haut dans la pile
                    } catch (ResponseStatusException rse) {
                         if (rse.getStatusCode() == HttpStatus.NOT_FOUND) {
                            log.warn("Block with ID {} (title: {}) was not found during update in synchronizeBlocks, possibly deleted concurrently. DTO details: {}", dto.getId(), dto.getTitle(), dto);
                         } else {
                            throw rse;
                         }
                    }

                } else {
                    // Le bloc référencé par le DTO (avec un ID non nul) n'existe plus.
                    log.warn("Block with ID {} (title: {}) not found during synchronization. It might have been deleted concurrently. Update ignored for this block.", dto.getId(), dto.getTitle());
                    // Stratégie: ignorer la mise à jour de ce bloc. La suppression par un autre processus prend le dessus.
                    // Si ce comportement n'est pas souhaité, une logique de "recréation" ou de notification pourrait être ajoutée ici.
                }
            } else { // Le DTO suggère une CRÉATION (id est null)
                BlockDTO dtoForCreate = prepareDtoForOperation(dto, targetModuleVersionId);
                createBlock(dtoForCreate);
            }
        }

        // Traiter les SUPPRESSIONS : blocs qui étaient en base (currentDbBlocksMap) mais ne sont pas dans les DTOs entrants avec un ID.
        Set<Long> dbIdsToDelete = currentDbBlocksMap.keySet().stream()
                .filter(dbId -> !incomingDtoNonNullIds.contains(dbId))
                .collect(Collectors.toSet());

        for (Long idToDelete : dbIdsToDelete) {
            deleteBlock(idToDelete);
        }
    }
    
    // Méthode utilitaire pour s'assurer que le DTO est correctement configuré pour l'opération
    private BlockDTO prepareDtoForOperation(BlockDTO originalDto, Long targetModuleVersionId) {
        // Crée une copie (ou modifie si mutable et sûr) pour s'assurer que moduleVersionId est correct.
        // Ceci est important si le DTO original n'avait pas de moduleVersionId (pour création)
        // ou si on veut forcer son association avec le targetModuleVersionId.
        
        // Pour les records (immutables), il faudrait recréer le DTO.
        // Si BlockDTO est une classe avec setters :
        // originalDto.setModuleVersionId(targetModuleVersionId); // Attention si originalDto est partagé.
        // Il est plus sûr de créer un nouveau DTO ou de s'assurer que le mapper le gère.

        // Exemple avec recréation pour différents types de DTOs (si ce sont des classes avec constructeurs appropriés)
        // Ceci suppose que BlockDTO est une superclasse et que vous avez des sous-classes spécifiques.
        BlockDTO preparedDto;
        if (originalDto instanceof ParagraphBlockDTO pDto) {
            preparedDto = new ParagraphBlockDTO(pDto.getParagraph(), pDto.getStyle(), pDto.getId(),
                    targetModuleVersionId, pDto.getTitle(), pDto.getBlockOrder(), pDto.getCreator());
        } else if (originalDto instanceof MusicBlockDTO mDto) {
            preparedDto = new MusicBlockDTO(mDto.getLabel(), mDto.getSrc(), mDto.getId(), targetModuleVersionId,
                    mDto.getTitle(), mDto.getBlockOrder(), mDto.getCreator());
        } else if (originalDto instanceof StatBlockDTO sDto) {
            preparedDto = new StatBlockDTO(sDto.getId(), targetModuleVersionId, sDto.getTitle(), sDto.getBlockOrder(),
                    sDto.getCreator(), sDto.getStatRules(), sDto.getStatValues());
        } else if (originalDto instanceof IntegratedModuleBlockDTO iDto) {
            preparedDto = new IntegratedModuleBlockDTO(iDto.getModuleId(), iDto.getId(), targetModuleVersionId,
                    iDto.getTitle(), iDto.getBlockOrder(), iDto.getCreator());
        } else if (originalDto instanceof PictureBlockDTO picDto) {
            preparedDto = new PictureBlockDTO(picDto.getLabel(), picDto.getPicture(), picDto.getId(),
                    targetModuleVersionId, picDto.getTitle(), picDto.getBlockOrder(), picDto.getCreator());
        } else {
            // Fallback générique ou erreur si le type n'est pas géré explicitement
            // Cela dépend de si BlockDTO lui-même a un setter pour moduleVersionId
            // Pour cet exemple, on suppose qu'on ne peut pas modifier directement ou qu'on préfère recréer.
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Type de BlockDTO non supporté pour la préparation : " + originalDto.getClass().getName());
        }
        // Copier les champs communs si non couverts par les constructeurs spécifiques
        preparedDto.setId(originalDto.getId()); // Important pour les mises à jour
        preparedDto.setCreator(originalDto.getCreator());
        preparedDto.setEntityVersion(originalDto.getEntityVersion()); // Conserver la version pour la logique de concurrence
        preparedDto.setLastModified(originalDto.getLastModified());
        // Ne pas copier createdAt/updatedAt, ils seront gérés par JPA ou la logique de service.
        return preparedDto;
    }


    public void createOrUpdateBlocks(List<BlockDTO> blockDTOs) {
        if (blockDTOs == null) {
            return;
        }
        for (BlockDTO blockDTO : blockDTOs) {
            if (blockDTO.getId() != null) {
                updateBlock(blockDTO.getId(), blockDTO);
            } else {
                createBlock(blockDTO);
            }
        }
    }
}