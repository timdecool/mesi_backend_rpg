package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleVersionMapper;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ModuleVersionService {

    private final ModuleVersionRepository moduleVersionRepository;
    private final ModuleVersionMapper moduleVersionMapper;
    public ModuleVersionService(
            ModuleVersionRepository moduleVersionRepository,
            ModuleVersionMapper moduleVersionMapper
    ) {
        this.moduleVersionRepository = moduleVersionRepository;
        this.moduleVersionMapper = moduleVersionMapper;
    }

    public ModuleVersionDTO findById(Long id) {
        ModuleVersion moduleVersion = moduleVersionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found"));
        return moduleVersionMapper.toDTO(moduleVersion);
    }

    public ModuleVersionDTO createVersion(Module module, ModuleVersionDTO moduleVersionDTO) {
        ModuleVersion version = moduleVersionMapper.toEntity(moduleVersionDTO);
        version.setModule(module);
        ModuleVersion savedVersion = moduleVersionRepository.save(version);
        return moduleVersionMapper.toDTO(savedVersion);
    }

    public ModuleVersionDTO updateVersion(ModuleVersionDTO moduleVersionDTO, Long id) {
        ModuleVersion version = moduleVersionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found"));;
        ModuleVersion newVersion = moduleVersionMapper.toEntity(moduleVersionDTO);
        newVersion.setId(version.getId());
        newVersion.setModule(version.getModule());
        newVersion.setCreatedAt(version.getCreatedAt());
        ModuleVersion savedVersion = moduleVersionRepository.save(newVersion);
        return moduleVersionMapper.toDTO(savedVersion);
    }
}
