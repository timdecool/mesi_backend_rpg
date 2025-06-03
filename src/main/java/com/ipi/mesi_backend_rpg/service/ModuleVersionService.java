package com.ipi.mesi_backend_rpg.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleVersionMapper;
import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import java.time.LocalDateTime;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleVersionService {

    private final ModuleVersionRepository moduleVersionRepository;
    private final ModuleVersionMapper moduleVersionMapper;
    private final BlockService blockService;
    private final UserRepository userRepository;
    private final GameSystemRepository gameSystemRepository;

    public ModuleVersionDTO findById(Long id) {
        ModuleVersion moduleVersion = moduleVersionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found"));
        return moduleVersionMapper.toDTO(moduleVersion);
    }

    public ModuleVersionDTO createVersion(Module module, ModuleVersionDTO moduleVersionDTO) {
        ModuleVersion version = moduleVersionMapper.toEntity(moduleVersionDTO);
        version.setModule(module);

        if (version.getLversion() == null) {
            version.setLversion(0L); // Hibernate commencera le versioning à 0
        }

        ModuleVersion savedVersion = moduleVersionRepository.save(version);
        return moduleVersionMapper.toDTO(savedVersion);
    }

    public ModuleVersionDTO updateVersion(ModuleVersionDTO moduleVersionDTO, Long id) {
        ModuleVersion version = moduleVersionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found"));

        if (moduleVersionDTO.entityVersion() != null &&
                !version.getLversion().equals(moduleVersionDTO.entityVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La version a été modifiée par un autre utilisateur. " +
                            "Veuillez recharger la page et réessayer.");
        }

        try {
            ModuleVersion newVersion = moduleVersionMapper.toEntity(moduleVersionDTO);
            newVersion.setId(version.getId());
            newVersion.setModule(version.getModule());
            newVersion.setCreatedAt(version.getCreatedAt());

            newVersion.setLversion(version.getLversion());
            newVersion.setPublished(moduleVersionDTO.published());
            newVersion.setLanguage(moduleVersionDTO.language());
            newVersion.setUpdatedAt(LocalDateTime.now());

            // Mettre à jour le créateur si nécessaire
            if (!newVersion.getCreator().getId().equals(moduleVersionDTO.creator().id())) {
                User newCreator = userRepository.findById(moduleVersionDTO.creator().id())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid user id: " + moduleVersionDTO.creator().id()));
                newVersion.setCreator(newCreator);
            }

            // Mettre à jour le système de jeu si nécessaire
            if (!newVersion.getGameSystem().getId().equals(moduleVersionDTO.gameSystemId())) {
                GameSystem newGameSystem = gameSystemRepository.findById(moduleVersionDTO.gameSystemId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid game system id: " + moduleVersionDTO.gameSystemId()));
                newVersion.setGameSystem(newGameSystem);
            }

            // Synchroniser les blocs si nécessaire
            if (moduleVersionDTO.blocks() != null && !moduleVersionDTO.blocks().isEmpty()) {
                blockService.synchronizeBlocks(newVersion.getId(), moduleVersionDTO.blocks());
            }

            ModuleVersion savedVersion = moduleVersionRepository.save(newVersion);
            return moduleVersionMapper.toDTO(savedVersion);

        } catch (OptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Conflit de modification détecté. Un autre utilisateur a modifié cette version " +
                            "pendant votre édition. Veuillez recharger la page et réappliquer vos modifications.");
        }
    }

    public void deleteVersion(Long id) {
        ModuleVersion version = moduleVersionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found"));

        Module module = version.getModule();
        if (module.getVersions().size() == 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This module has no other version");
        }

        moduleVersionRepository.delete(version);
    }

    public List<ModuleVersionDTO> findAllByModule(Module module) {
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "module not found");
        }

        return moduleVersionRepository.findAllByModule(module).stream().map(moduleVersionMapper::toDTO).toList();
    }

    public ModuleVersion toEntity(ModuleVersionDTO dto) {
        return moduleVersionRepository.findById(dto.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Module version not found with id: " + dto.id()));
    }

    public void synchronizeModuleVersion(List<ModuleVersionDTO> incomingVersionDTOs, Module module) {
        if (module == null) {
            throw new IllegalArgumentException("Module cannot be null for synchronizing versions.");
        }

        List<ModuleVersionDTO> dtosToProcess = (incomingVersionDTOs == null) ? new ArrayList<>() : incomingVersionDTOs;

        Map<Long, ModuleVersion> currentDbVersionsMap = moduleVersionRepository.findAllByModule(module).stream()
                .collect(Collectors.toMap(ModuleVersion::getId, Function.identity()));

        Set<Long> incomingIdsWithNonNullId = dtosToProcess.stream()
                .map(ModuleVersionDTO::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Traiter les créations et les mises à jour
        for (ModuleVersionDTO dto : dtosToProcess) {
            if (dto.id() != null) { // DTO suggère une mise à jour
                if (currentDbVersionsMap.containsKey(dto.id())) {
                    ModuleVersion versionToUpdate = currentDbVersionsMap.get(dto.id());
                    if (versionToUpdate.getModule().getId() != module.getId()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Version with id " + dto.id() + " for update does not belong to module "
                                        + module.getId() + ".");
                    }
                    updateVersion(dto, dto.id());
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Version with id " + dto.id() + " provided for update but not found for module "
                                    + module.getId() + ".");
                }
            } else { // Pas d'ID dans le DTO, donc c'est une création
                createVersion(module, dto);
            }
        }

        // Traiter les suppressions
        Set<Long> dbIdsToDelete = currentDbVersionsMap.keySet().stream()
                .filter(dbId -> !incomingIdsWithNonNullId.contains(dbId))
                .collect(Collectors.toSet());

        for (Long idToDelete : dbIdsToDelete) {
            deleteVersion(idToDelete);
        }
    }
}