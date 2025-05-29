package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseSummaryDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleMapper;
import com.ipi.mesi_backend_rpg.mapper.PictureMapper;
import com.ipi.mesi_backend_rpg.model.*;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import com.ipi.mesi_backend_rpg.repository.UserSavedModuleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;
    private final PictureMapper pictureMapper;

    private final ModuleVersionService moduleVersionService;

    private final ModuleAccessService moduleAccessService;

    private final GameSystemRepository gameSystemRepository;
    private final UserRepository userRepository;

    private final UserSavedModuleRepository userSavedModuleRepository;
    private final UserService userService;

    public List<ModuleResponseDTO> findAllModules() {
        return moduleRepository.findAll().stream().map(moduleMapper::toDTO).toList();
    }

    public ModuleResponseDTO findById(Long id) {
        Optional<Module> module = moduleRepository.findById(id);
        return module.map(moduleMapper::toDTO).orElse(null);
    }

    public ModuleResponseSummaryDTO findModuleSummaryById(Long id) {
        Optional<Module> module = moduleRepository.findById(id);
        return module.map(moduleMapper::toSummaryDTO).orElse(null);
    }

    public ModuleResponseDTO createModule(ModuleRequestDTO moduleRequestDTO) {
        Module module = moduleMapper.toEntity(moduleRequestDTO);
        User user = userService.getAuthenticatedUser();
        module.setCreator(user);

        GameSystem gameSystem = gameSystemRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Default gameSystem not found"));

        // Créer la 1ere version du module
        ModuleVersion initialModuleVersion = new ModuleVersion();
        initialModuleVersion.setVersion(1);
        initialModuleVersion.setCreator(user);
        initialModuleVersion.setPublished(false);
        initialModuleVersion.setGameSystem(gameSystem);
        initialModuleVersion.setLanguage("");
        initialModuleVersion.setModule(module);
        module.addVersion(initialModuleVersion);

        // Créer le moduleAcess du creator avec tous les droits
        ModuleAccess initialModuleAccess = new ModuleAccess();
        initialModuleAccess.setUser(user);
        initialModuleAccess.setCanEdit(true);
        initialModuleAccess.setCanView(true);
        initialModuleAccess.setCanPublish(true);
        initialModuleAccess.setCanInvite(true);
        initialModuleAccess.setModule(module);
        module.addAccess(initialModuleAccess);

        Module savedModule = moduleRepository.save(module);

        return moduleMapper.toDTO(savedModule);
    }

    // Méthode pour mettre à jour un module avec ses versions et accès
    public ModuleResponseDTO updateModule(Long id, ModuleRequestDTO moduleRequestDTO, Long userId) {
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

        // 3. Mettre à jour les versions si fournies
        if (moduleRequestDTO.versions() != null && !moduleRequestDTO.versions().isEmpty()) {
            moduleVersionService.synchronizeModuleVersion(moduleRequestDTO.versions(), existingModule);
        }

        // 4. Mettre à jour les accès si fournis
        if (moduleRequestDTO.accesses() != null && !moduleRequestDTO.accesses().isEmpty()) {
            moduleAccessService.synchronizeModuleAccesses(existingModule, moduleRequestDTO.accesses(), userId);
        }

        moduleRepository.save(existingModule);

        return findById(existingModule.getId());
    }

    @Transactional
    public void deleteModule(Long id) {
        userSavedModuleRepository.deleteByModule_Id(id);
    }

    public List<ModuleResponseDTO> searchModules(String query) {
        PageRequest pageable = PageRequest.of(0, 30); // Les 30 premiers résultats (page 0, taille 30)
        List<Module> modules = moduleRepository.findByTitleOrDescriptionContainingIgnoreCase(query, pageable);

        if (modules.isEmpty()) {
            return List.of(); // Retourne une liste vide si aucun résultat
        }
        return modules.stream()
                .map(moduleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ModuleResponseDTO> getMostSavedModules(int page, int limit) {
        return moduleRepository.findMostSavedModules(PageRequest.of(page, limit))
                .stream().map(moduleMapper::toDTO).collect(Collectors.toList());
    }

    public List<ModuleResponseDTO> getMostRecentModules(int page, int limit) {
        return moduleRepository.findMostRecentModules(PageRequest.of(page, limit))
                .stream().map(moduleMapper::toDTO).collect(Collectors.toList());
    }

    public List<ModuleResponseDTO> getMostCommentedModules(int limit, int page) {
        return moduleRepository.findMostCommentedModules(PageRequest.of(page, limit))
                .stream().map(moduleMapper::toDTO).collect(Collectors.toList());
    }
}