package com.ipi.mesi_backend_rpg.service;

import java.time.LocalDateTime; // N'oubliez pas cet import
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import pour la transaction
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleVersionMapper;
import com.ipi.mesi_backend_rpg.model.GameSystem; // Import du modèle GameSystem
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository; // Import du repository
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuleVersionService {

    private final ModuleVersionRepository moduleVersionRepository;
    private final ModuleVersionMapper moduleVersionMapper;
    private final BlockService blockService;
    private final UserService userService;
    private final GameSystemRepository gameSystemRepository; // Injection de la dépendance

    public ModuleVersionDTO findById(Long id) {
        ModuleVersion moduleVersion = moduleVersionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found"));
        return moduleVersionMapper.toDTO(moduleVersion);
    }

    @Transactional
    public ModuleVersionDTO createVersion(Module module, ModuleVersionDTO moduleVersionDTO) {
        ModuleVersion version = moduleVersionMapper.toEntity(moduleVersionDTO);
        version.setModule(module);
        version.setCreator(userService.getAuthenticatedUser());
        ModuleVersion savedVersion = moduleVersionRepository.save(version);
        return moduleVersionMapper.toDTO(savedVersion);
    }

    @Transactional // Il est bon de rendre cette méthode transactionnelle
    public ModuleVersionDTO updateVersion(ModuleVersionDTO moduleVersionDTO, Long id) {
        // 1. On récupère l'entité existante depuis la base de données
        ModuleVersion existingVersion = moduleVersionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found with id: " + id));

        // 2. On met à jour les propriétés de l'entité existante avec les données du DTO
        existingVersion.setPublished(moduleVersionDTO.published());
        existingVersion.setLanguage(moduleVersionDTO.language());
        existingVersion.setUpdatedAt(LocalDateTime.now());

        if (moduleVersionDTO.gameSystemId() != null) {
            GameSystem gameSystem = gameSystemRepository.findById(moduleVersionDTO.gameSystemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid gameSystemId: " + moduleVersionDTO.gameSystemId()));
            existingVersion.setGameSystem(gameSystem);
        }

        // 3. On appelle la synchronisation des blocs avec la liste de DTOs reçue directement
        if (moduleVersionDTO.blocks() != null) {
            blockService.synchronizeBlocks(existingVersion.getId(), moduleVersionDTO.blocks());
        }

        // 4. On sauvegarde l'entité qu'on a modifiée (et qui est toujours gérée par Hibernate)
        ModuleVersion savedVersion = moduleVersionRepository.save(existingVersion);
        
        // 5. On retourne un DTO frais à partir de l'entité sauvegardée
        return moduleVersionMapper.toDTO(savedVersion);
    }

    public void deleteVersion(Long id) {
        ModuleVersion version = moduleVersionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found"));

        Module module = version.getModule();
        if (module.getVersions().size() <= 1) { // Logique de sécurité améliorée
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This module must have at least one version.");
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

    @Transactional
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

        for (ModuleVersionDTO dto : dtosToProcess) {
            if (dto.id() != null) {
                if (currentDbVersionsMap.containsKey(dto.id())) {
                    updateVersion(dto, dto.id());
                } else {
                     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Version with id " + dto.id() + " provided for update but not found for module " + module.getId() + ".");
                }
            } else {
                createVersion(module, dto);
            }
        }

        Set<Long> dbIdsToDelete = currentDbVersionsMap.keySet().stream()
                .filter(dbId -> !incomingIdsWithNonNullId.contains(dbId))
                .collect(Collectors.toSet());

        for (Long idToDelete : dbIdsToDelete) {
            deleteVersion(idToDelete);
        }
    }
}