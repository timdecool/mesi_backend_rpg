package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleMapper;
import com.ipi.mesi_backend_rpg.model.GameSystem;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.GameSystemRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;
    private final GameSystemRepository gameSystemRepository;


    public List<ModuleResponseDTO> findAllModules() {
        return moduleRepository.findAll().stream().map(moduleMapper::toDTO).toList();
    }

    public ModuleResponseDTO findById(Long id) {
        Optional<Module> module = moduleRepository.findById(id);
        return module.map(moduleMapper::toDTO).orElse(null);
    }

    public ModuleResponseDTO createModule(ModuleRequestDTO moduleRequestDTO) {
        Module module = moduleMapper.toEntity(moduleRequestDTO);
        GameSystem gameSystem = gameSystemRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("Invalid gameSystem"));
        
        ModuleVersion moduleVersion = new ModuleVersion();
        moduleVersion.setModule(module);
        moduleVersion.setVersion(1);
        moduleVersion.setCreatedBy("author");
        moduleVersion.setPublished(false);
        moduleVersion.setGameSystem(gameSystem);
        moduleVersion.setLanguage("");
        module.addVersion(moduleVersion);


        Module savedModule = moduleRepository.save(module);
        return moduleMapper.toDTO(savedModule);
    }

    public ModuleResponseDTO updateModule(Long id, @Valid ModuleRequestDTO moduleRequestDTO) {
        Module module = moduleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module not found"));
        Module savedModule = moduleMapper.toEntity(moduleRequestDTO);
        savedModule.setId(module.getId());
        savedModule.setCreatedAt(module.getCreatedAt());
        savedModule = moduleRepository.save(savedModule);
        return moduleMapper.toDTO(savedModule);
    }

    public void deleteModule(Long id) {
        moduleRepository.findById(id).ifPresent(moduleRepository::delete);
    }

}
