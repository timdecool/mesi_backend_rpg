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

    private final UserSavedModuleRepository userSavedModuleRepository;
    private final UserService userService;

    public List<ModuleResponseDTO> findAllModules() {
        return moduleRepository.findAll().stream().map(moduleMapper::toDTO).toList();
    }

    public ModuleResponseDTO findById(Long id) {
        Optional<Module> module = moduleRepository.findById(id);
        return module.map(moduleMapper::toDTO).orElse(null);
    }

    public List<ModuleResponseSummaryDTO> findAllModuleSummaryByUserId(Long id) {
        List<Module> modules = moduleRepository.findAllByCreator_Id(id);
        return modules.stream().map(moduleMapper::toSummaryDTO).toList();
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
        moduleRepository.deleteById(id);
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

    public List<ModuleResponseDTO> getMostRatedModules(int page, int limit) {
        return moduleRepository.findMostRatedModules(PageRequest.of(page, limit))
                .stream().map(moduleMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public ModuleResponseDTO duplicateModule(Long moduleId) {
        Module originalModule = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        
        User currentUser = userService.getAuthenticatedUser();
        
        Module duplicatedModule = new Module();
        duplicatedModule.setTitle(originalModule.getTitle() + " (Copie)");
        duplicatedModule.setDescription(originalModule.getDescription());
        duplicatedModule.setIsTemplate(originalModule.getIsTemplate());
        duplicatedModule.setType(originalModule.getType());
        duplicatedModule.setCreator(currentUser);
        duplicatedModule.setCreatedAt(LocalDateTime.now());
        duplicatedModule.setUpdatedAt(LocalDateTime.now());
        
        if (originalModule.getPicture() != null) {
            Picture duplicatedPicture = new Picture();
            duplicatedPicture.setTitle(originalModule.getPicture().getTitle());
            duplicatedPicture.setSrc(originalModule.getPicture().getSrc());
            duplicatedPicture.setCreatedAt(LocalDateTime.now());
            duplicatedPicture.setUpdatedAt(LocalDateTime.now());
            duplicatedModule.setPicture(duplicatedPicture);
        }
        
        GameSystem gameSystem = gameSystemRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Default gameSystem not found"));
        
        ModuleVersion initialModuleVersion = new ModuleVersion();
        initialModuleVersion.setVersion(1);
        initialModuleVersion.setCreator(currentUser);
        initialModuleVersion.setPublished(false);
        initialModuleVersion.setGameSystem(gameSystem);
        initialModuleVersion.setLanguage("");
        initialModuleVersion.setModule(duplicatedModule);
        
        for (ModuleVersion originalVersion : originalModule.getVersions()) {
            if (originalVersion.getBlocks() != null) {
                for (Block originalBlock : originalVersion.getBlocks()) {
                    Block duplicatedBlock = duplicateBlock(originalBlock);
                    duplicatedBlock.setModuleVersion(initialModuleVersion);
                    initialModuleVersion.addBlock(duplicatedBlock);
                }
            }
        }
        
        duplicatedModule.addVersion(initialModuleVersion);
        
        ModuleAccess initialModuleAccess = new ModuleAccess();
        initialModuleAccess.setUser(currentUser);
        initialModuleAccess.setCanEdit(true);
        initialModuleAccess.setCanView(true);
        initialModuleAccess.setCanPublish(true);
        initialModuleAccess.setCanInvite(true);
        initialModuleAccess.setModule(duplicatedModule);
        duplicatedModule.addAccess(initialModuleAccess);
        
        Module savedModule = moduleRepository.save(duplicatedModule);
        return moduleMapper.toDTO(savedModule);
    }
    
    private Block duplicateBlock(Block originalBlock) {
        Block duplicatedBlock;
        
        if (originalBlock instanceof ParagraphBlock) {
            ParagraphBlock originalParagraph = (ParagraphBlock) originalBlock;
            ParagraphBlock duplicatedParagraph = new ParagraphBlock();
            duplicatedParagraph.setParagraph(originalParagraph.getParagraph());
            duplicatedParagraph.setStyle(originalParagraph.getStyle());
            duplicatedBlock = duplicatedParagraph;
        } else if (originalBlock instanceof PictureBlock) {
            PictureBlock originalPicture = (PictureBlock) originalBlock;
            PictureBlock duplicatedPicture = new PictureBlock();
            duplicatedPicture.setLabel(originalPicture.getLabel());
            if (originalPicture.getPicture() != null) {
                Picture newPicture = new Picture();
                newPicture.setTitle(originalPicture.getPicture().getTitle());
                newPicture.setSrc(originalPicture.getPicture().getSrc());
                duplicatedPicture.setPicture(newPicture);
            }
            duplicatedBlock = duplicatedPicture;
        } else if (originalBlock instanceof MusicBlock) {
            MusicBlock originalMusic = (MusicBlock) originalBlock;
            MusicBlock duplicatedMusic = new MusicBlock();
            duplicatedMusic.setLabel(originalMusic.getLabel());
            duplicatedMusic.setSrc(originalMusic.getSrc());
            duplicatedBlock = duplicatedMusic;
        } else if (originalBlock instanceof StatBlock) {
            StatBlock originalStat = (StatBlock) originalBlock;
            StatBlock duplicatedStat = new StatBlock();
            duplicatedStat.setStatRules(originalStat.getStatRules());
            duplicatedStat.setStatValues(originalStat.getStatValues());
            duplicatedBlock = duplicatedStat;
        } else {
            duplicatedBlock = new Block();
        }
        
        duplicatedBlock.setTitle(originalBlock.getTitle());
        duplicatedBlock.setBlockOrder(originalBlock.getBlockOrder());
        duplicatedBlock.setType(originalBlock.getType());
        
        return duplicatedBlock;
    }
}