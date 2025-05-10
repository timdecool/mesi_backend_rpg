package com.ipi.mesi_backend_rpg.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.mapper.BlockMapper;
import com.ipi.mesi_backend_rpg.mapper.ModuleVersionMapper;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuleVersionService {

    private final ModuleVersionRepository moduleVersionRepository;
    private final ModuleVersionMapper moduleVersionMapper;

    private final BlockService blockService;
    private final BlockMapper blockMapper;

    public ModuleVersionDTO findById(Long id) {
        ModuleVersion moduleVersion = moduleVersionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found"));
        return moduleVersionMapper.toDTO(moduleVersion);
    }

    public ModuleVersionDTO createVersion(Module module, ModuleVersionDTO moduleVersionDTO) {
        ModuleVersion version = moduleVersionMapper.toEntity(moduleVersionDTO);
        version.setModule(module);
        ModuleVersion savedVersion = moduleVersionRepository.save(version);
        return moduleVersionMapper.toDTO(savedVersion);
    }

    public ModuleVersionDTO updateVersion(ModuleVersionDTO moduleVersionDTO, Long id) {
        ModuleVersion version = moduleVersionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found"));
        ModuleVersion newVersion = moduleVersionMapper.toEntity(moduleVersionDTO);
        newVersion.setId(version.getId());
        newVersion.setModule(version.getModule());
        newVersion.setCreatedAt(version.getCreatedAt());

        if (newVersion.getBlocks() != null && !newVersion.getBlocks().isEmpty()) {
            blockService.synchronizeBlocks(newVersion.getId(),
                    newVersion.getBlocks().stream().map(blockMapper::toDTO).collect(Collectors.toList()));
        }

        ModuleVersion savedVersion = moduleVersionRepository.save(newVersion);
        return moduleVersionMapper.toDTO(savedVersion);
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
        // Traiter une liste nulle de DTOs comme une liste vide pour la logique de suppression
        List<ModuleVersionDTO> dtosToProcess = (incomingVersionDTOs == null) ? new ArrayList<>() : incomingVersionDTOs;

        // Récupérer les versions actuelles en BD pour ce module
        Map<Long, ModuleVersion> currentDbVersionsMap = moduleVersionRepository.findAllByModule(module).stream()
                .collect(Collectors.toMap(ModuleVersion::getId, Function.identity()));

        // Identifier les IDs des DTOs entrants qui ont un ID (pour les mises à jour)
        Set<Long> incomingIdsWithNonNullId = dtosToProcess.stream()
                .map(ModuleVersionDTO::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Traiter les créations et les mises à jour
        for (ModuleVersionDTO dto : dtosToProcess) {
            if (dto.id() != null) { // DTO suggère une mise à jour
                if (currentDbVersionsMap.containsKey(dto.id())) {
                    // Vérifier que la version appartient bien au module concerné
                    ModuleVersion versionToUpdate = currentDbVersionsMap.get(dto.id());
                    if (versionToUpdate.getModule().getId() != module.getId()) {
                         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                            "Version with id " + dto.id() + " for update does not belong to module " + module.getId() + ".");
                    }
                    updateVersion(dto, dto.id());
                } else {
                    // DTO a un ID, mais il n'existe pas pour ce module en BD.
                    // Soit une erreur client, soit tentative de "créer avec ID spécifique" (généralement non permis).
                     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Version with id " + dto.id() + " provided for update but not found for module " + module.getId() + ".");
                }
            } else { // Pas d'ID dans le DTO, donc c'est une création
                createVersion(module, dto);
            }
        }

        // Traiter les suppressions : versions en BD non présentes dans les DTOs entrants (par ID)
        Set<Long> dbIdsToDelete = currentDbVersionsMap.keySet().stream()
                .filter(dbId -> !incomingIdsWithNonNullId.contains(dbId))
                .collect(Collectors.toSet());

        for (Long idToDelete : dbIdsToDelete) {
            deleteVersion(idToDelete);
        }
    }
}
