package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleMapper;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;
    public ModuleService(ModuleRepository moduleRepository, ModuleMapper moduleMapper) {
        this.moduleRepository = moduleRepository;
        this.moduleMapper = moduleMapper;
    }

    public List<ModuleResponseDTO> findAllModules() {
        return moduleRepository.findAll().stream().map(moduleMapper::toDTO).toList();
    }

    public ModuleResponseDTO findById(Long id) {
        Optional<Module> module = moduleRepository.findById(id);
        return module.map(moduleMapper::toDTO).orElse(null);
    }

    public ModuleResponseDTO createModule(ModuleRequestDTO moduleRequestDTO) {
    }

    public ModuleResponseDTO updateModule(Long id, @Valid ModuleRequestDTO moduleRequestDTO) {
    }

    public void deleteModule(Long id) {
    }

}
