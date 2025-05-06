package com.ipi.mesi_backend_rpg.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.BlockDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleAccessDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.dto.MusicBlockDTO;
import com.ipi.mesi_backend_rpg.dto.ParagraphBlockDTO;
import com.ipi.mesi_backend_rpg.dto.StatBlockDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleMapper;
import com.ipi.mesi_backend_rpg.mapper.PictureMapper;
import com.ipi.mesi_backend_rpg.model.Block;
import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.model.IntegratedModuleBlock;
import com.ipi.mesi_backend_rpg.model.ModuleAccess;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.model.MusicBlock;
import com.ipi.mesi_backend_rpg.model.ParagraphBlock;
import com.ipi.mesi_backend_rpg.model.StatBlock;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.BlockRepository;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleAccessRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import com.ipi.mesi_backend_rpg.model.Module;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;
    private final PictureMapper pictureMapper;
    private final ModuleVersionRepository moduleVersionRepository;
    private final ModuleAccessRepository moduleAccessRepository;
    private final GameSystemRepository gameSystemRepository;
    private final UserRepository userRepository;

    // blocks
    private final BlockRepository blockRepository;

    public List<ModuleResponseDTO> findAllModules() {
        return moduleRepository.findAll().stream().map(moduleMapper::toDTO).toList();
    }

    public ModuleResponseDTO findById(Long id) {
        Optional<Module> module = moduleRepository.findById(id);
        return module.map(moduleMapper::toDTO).orElse(null);
    }

    public ModuleResponseDTO createModule(ModuleRequestDTO moduleRequestDTO) {
        // 1. Créer et sauvegarder le module de base
        com.ipi.mesi_backend_rpg.model.Module module = moduleMapper.toEntity(moduleRequestDTO);
        module = moduleRepository.save(module);

        // 2. Créer la version initiale
        GameSystem gameSystem = gameSystemRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Default gameSystem not found"));

        ModuleVersion moduleVersion = new ModuleVersion();
        moduleVersion.setModule(module);
        moduleVersion.setVersion(1);
        moduleVersion.setCreator(userRepository.findById(module.getCreator().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user")));
        moduleVersion.setPublished(false);
        moduleVersion.setGameSystem(gameSystem);
        moduleVersion.setLanguage("");

        moduleVersion = moduleVersionRepository.save(moduleVersion);

        if (module.getVersions() == null) {
            module.setVersions(new ArrayList<>());
        }
        module.getVersions().add(moduleVersion);

        // 3. Créer l'accès pour le créateur
        ModuleAccess moduleAccess = new ModuleAccess();
        moduleAccess.setModule(module);
        moduleAccess.setUser(module.getCreator());
        moduleAccess.setCanView(true);
        moduleAccess.setCanEdit(true);
        moduleAccess.setCanInvite(true);
        moduleAccess.setCanPublish(true);

        moduleAccess = moduleAccessRepository.save(moduleAccess);

        if (module.getAccesses() == null) {
            module.setAccesses(new ArrayList<>());
        }
        module.getAccesses().add(moduleAccess);

        return moduleMapper.toDTO(module);
    }

    // Méthode pour mettre à jour un module avec ses versions et accès
    public ModuleResponseDTO updateModule(Long id, ModuleRequestDTO moduleRequestDTO) {
        Module existingModule = moduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        // 1. Mettre à jour les propriétés de base du module
        existingModule.setTitle(moduleRequestDTO.title());
        existingModule.setDescription(moduleRequestDTO.description());
        existingModule.setIsTemplate(moduleRequestDTO.isTemplate());
        existingModule.setType(moduleRequestDTO.type());
        existingModule.setUpdatedAt(LocalDateTime.now());

        // 2. Mettre à jour l'image si fournie
        if (moduleRequestDTO.picture() != null) {
            if (existingModule.getPicture() != null) {
                existingModule.getPicture().setTitle(moduleRequestDTO.picture().title());
                existingModule.getPicture().setSrc(moduleRequestDTO.picture().src());
                existingModule.getPicture().setUpdatedAt(LocalDateTime.now());
            } else {
                existingModule.setPicture(pictureMapper.toEntity(moduleRequestDTO.picture()));
            }
        }

        moduleRepository.save(existingModule);

        // 3. Mettre à jour les versions si fournies
        if (moduleRequestDTO.versions() != null && !moduleRequestDTO.versions().isEmpty()) {
            updateModuleVersions(existingModule, moduleRequestDTO.versions());
        }

        moduleRepository.save(existingModule);

        // 4. Mettre à jour les accès si fournis
        if (moduleRequestDTO.accesses() != null && !moduleRequestDTO.accesses().isEmpty()) {
            updateModuleAccesses(existingModule, moduleRequestDTO.accesses());
        }

        moduleRepository.save(existingModule);

        return findById(existingModule.getId());
    }

    private void updateModuleVersions(com.ipi.mesi_backend_rpg.model.Module module,
            List<ModuleVersionDTO> versionDTOs) {
        // Obtenir les versions existantes
        List<ModuleVersion> existingVersions = moduleVersionRepository.findAllByModule(module);
        Map<Long, ModuleVersion> existingVersionMap = existingVersions.stream()
                .collect(Collectors.toMap(ModuleVersion::getId, v -> v));

        // Traiter chaque version du DTO
        for (ModuleVersionDTO versionDTO : versionDTOs) {
            if (versionDTO.id() != null && existingVersionMap.containsKey(versionDTO.id())) {
                // Mise à jour d'une version existante
                ModuleVersion version = existingVersionMap.get(versionDTO.id());
                version.setVersion(versionDTO.version());
                version.setPublished(versionDTO.published());
                version.setLanguage(versionDTO.language());
                version.setUpdatedAt(LocalDateTime.now());

                if (versionDTO.gameSystemId() != null) {
                    GameSystem gameSystem = gameSystemRepository.findById(versionDTO.gameSystemId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid gameSystem"));
                    version.setGameSystem(gameSystem);
                }

                // Mettre à jour les blocks si fournis
                if (versionDTO.blocks() != null && !versionDTO.blocks().isEmpty()) {
                    updateBlocksForVersion(version, versionDTO.blocks());
                }

                moduleVersionRepository.save(version);
                existingVersionMap.remove(versionDTO.id());
            } else {
                // Création d'une nouvelle version
                ModuleVersion newVersion = new ModuleVersion();
                newVersion.setModule(module);
                newVersion.setVersion(versionDTO.version());
                newVersion.setCreator(userRepository.findById(versionDTO.creator().id())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid user")));
                newVersion.setPublished(versionDTO.published());
                newVersion.setLanguage(versionDTO.language());

                GameSystem gameSystem = gameSystemRepository.findById(versionDTO.gameSystemId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid gameSystem"));
                newVersion.setGameSystem(gameSystem);

                newVersion = moduleVersionRepository.save(newVersion);

                // Ajouter les blocks si fournis
                if (versionDTO.blocks() != null && !versionDTO.blocks().isEmpty()) {
                    updateBlocksForVersion(newVersion, versionDTO.blocks());
                }
            }
        }
    }

    private void updateBlocksForVersion(ModuleVersion version, List<BlockDTO> blockDTOs) {
        // Obtenir les blocks existants pour cette version
        List<Block> existingBlocks = blockRepository.findAllByModuleVersion(version);

        // Créer une map des blocks existants par ID
        Map<Long, Block> existingBlockMap = existingBlocks.stream()
                .collect(Collectors.toMap(Block::getId, b -> b, (b1, b2) -> b1));

        // Conserver les IDs des blocks mis à jour/conservés
        List<Long> retainedBlockIds = new ArrayList<>();

        for (BlockDTO blockDTO : blockDTOs) {
            if (blockDTO == null)
                continue;

            Long blockId = blockDTO.getId();
            // Vérifier si c'est un ID valide dans la base de données
            if (blockId != null && blockId > 0 && existingBlockMap.containsKey(blockId)) {
                // Block existant à mettre à jour
                Block block = existingBlockMap.get(blockId);

                // Vérifier si le type est cohérent
                if ((block instanceof ParagraphBlock && "paragraph".equals(blockDTO.getType())) ||
                        (block instanceof MusicBlock && "music".equals(blockDTO.getType())) ||
                        (block instanceof StatBlock && "stat".equals(blockDTO.getType())) ||
                        (block instanceof IntegratedModuleBlock && "module".equals(blockDTO.getType()))) {

                    // Mise à jour des propriétés communes
                    block.setTitle(blockDTO.getTitle());
                    block.setBlockOrder(blockDTO.getBlockOrder());
                    block.setUpdatedAt(LocalDate.now());

                    // Mise à jour spécifique selon le type
                    if (block instanceof ParagraphBlock && blockDTO instanceof ParagraphBlockDTO) {
                        updateParagraphBlock((ParagraphBlock) block, (ParagraphBlockDTO) blockDTO);
                    } else if (block instanceof MusicBlock && blockDTO instanceof MusicBlockDTO) {
                        updateMusicBlock((MusicBlock) block, (MusicBlockDTO) blockDTO);
                    } else if (block instanceof StatBlock && blockDTO instanceof StatBlockDTO) {
                        updateStatBlock((StatBlock) block, (StatBlockDTO) blockDTO);
                    }

                    blockRepository.save(block);
                    retainedBlockIds.add(blockId);
                } else {
                    // Type incompatible - supprimer l'ancien et créer un nouveau
                    blockRepository.delete(block);
                    Block newBlock = createNewBlock(version, blockDTO);
                    retainedBlockIds.add(newBlock.getId());
                }
            } else {
                // C'est un nouveau block ou un block avec un ID temporaire/invalide
                // Ignorer l'ID du DTO et en créer un nouveau
                blockDTO.setId(null); // Forcer la création d'un nouvel ID
                Block newBlock = createNewBlock(version, blockDTO);
                retainedBlockIds.add(newBlock.getId());
            }
        }

        // Supprimer les blocks qui n'existent plus dans la liste fournie
        existingBlocks.stream()
                .filter(block -> !retainedBlockIds.contains(block.getId()))
                .forEach(blockRepository::delete);
    }

    private Block createNewBlock(ModuleVersion version, BlockDTO blockDTO) {
        if (blockDTO == null)
            return null;

        // Récupérer l'utilisateur créateur
        User creator = userRepository.findById(blockDTO.getCreator().id())
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id: " + blockDTO.getCreator().id()));

        // Créer le bon type de block en fonction du type spécifié
        Block newBlock;
        String blockType = blockDTO.getType();

        switch (blockType) {
            case "paragraph":
                ParagraphBlock paragraphBlock = new ParagraphBlock();
                if (blockDTO instanceof ParagraphBlockDTO paragraphDTO) {
                    paragraphBlock.setParagraph(paragraphDTO.getParagraph());
                    paragraphBlock.setStyle(paragraphDTO.getStyle());
                }
                newBlock = paragraphBlock;
                break;

            case "music":
                MusicBlock musicBlock = new MusicBlock();
                if (blockDTO instanceof MusicBlockDTO musicDTO) {
                    musicBlock.setLabel(musicDTO.getLabel());
                    musicBlock.setSrc(musicDTO.getSrc());
                }
                newBlock = musicBlock;
                break;

            case "stat":
                StatBlock statBlock = new StatBlock();
                if (blockDTO instanceof StatBlockDTO statDTO) {
                    statBlock.setStatValues(statDTO.getStatValues());
                    statBlock.setStatRules(statDTO.getStatRules());
                }
                newBlock = statBlock;
                break;

            case "module":
                IntegratedModuleBlock moduleBlock = new IntegratedModuleBlock();
                // if (blockDTO instanceof IntegratedModuleBlockDTO moduleDTO) {

                // }
                newBlock = moduleBlock;
                break;

            default:
                throw new IllegalArgumentException("Unknown block type: " + blockType);
        }

        // Configurer les propriétés communes à tous les types de blocks
        newBlock.setModuleVersion(version);
        newBlock.setTitle(blockDTO.getTitle());
        newBlock.setBlockOrder(blockDTO.getBlockOrder());
        newBlock.setCreator(creator);
        newBlock.setCreatedAt(LocalDate.now());
        newBlock.setUpdatedAt(LocalDate.now());

        // Sauvegarder et retourner le nouveau block
        return blockRepository.save(newBlock);
    }

    // Méthodes auxiliaires pour mettre à jour chaque type de block
    private void updateParagraphBlock(ParagraphBlock block, ParagraphBlockDTO dto) {
        block.setParagraph(dto.getParagraph());
        block.setStyle(dto.getStyle());
    }

    private void updateMusicBlock(MusicBlock block, MusicBlockDTO dto) {
        block.setLabel(dto.getLabel());
        block.setSrc(dto.getSrc());
    }

    private void updateStatBlock(StatBlock block, StatBlockDTO dto) {
        block.setStatValues(dto.getStatValues());
        block.setStatRules(dto.getStatRules());
    }

    private void updateModuleAccesses(com.ipi.mesi_backend_rpg.model.Module module, List<ModuleAccessDTO> accessDTOs) {
        // Obtenir les accès existants
        List<ModuleAccess> existingAccesses = moduleAccessRepository.findAllByModule(module);
        Map<Integer, ModuleAccess> existingAccessMap = existingAccesses.stream()
                .collect(Collectors.toMap(ModuleAccess::getId, a -> a));

        // Traiter chaque accès du DTO
        for (ModuleAccessDTO accessDTO : accessDTOs) {
            if (accessDTO.id() != null && existingAccessMap.containsKey(accessDTO.id())) {
                // Mise à jour d'un accès existant
                ModuleAccess access = existingAccessMap.get(accessDTO.id());
                access.setCanView(accessDTO.canView());
                access.setCanEdit(accessDTO.canEdit());
                access.setCanPublish(accessDTO.canPublish());
                access.setCanInvite(accessDTO.canInvite());

                moduleAccessRepository.save(access);
                existingAccessMap.remove(accessDTO.id());
            } else {
                // Création d'un nouvel accès
                ModuleAccess newAccess = new ModuleAccess();
                newAccess.setModule(module);
                newAccess.setUser(userRepository.findById(accessDTO.user().id())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid user")));
                newAccess.setCanView(accessDTO.canView());
                newAccess.setCanEdit(accessDTO.canEdit());
                newAccess.setCanPublish(accessDTO.canPublish());
                newAccess.setCanInvite(accessDTO.canInvite());

                moduleAccessRepository.save(newAccess);
            }
        }
    }

    public void deleteModule(Long id) {
        moduleRepository.findById(id).ifPresent(moduleRepository::delete);
    }
}