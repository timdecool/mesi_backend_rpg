package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.ModuleAccessDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleAccessMapper;
import com.ipi.mesi_backend_rpg.repository.ModuleAccessRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModuleAccessService {

    ModuleAccessRepository moduleAccessRepository;
    ModuleAccessMapper moduleAccessMapper;

    public ModuleAccessService(ModuleAccessRepository moduleAccessRepository, ModuleAccessMapper moduleAccessMapper) {
        this.moduleAccessRepository = moduleAccessRepository;
        this.moduleAccessMapper = moduleAccessMapper;
    }

    public List<ModuleAccessDTO> getAllModuleAccesses() {
        return moduleAccessRepository.findAll()
                .stream()
                .map(moduleAccessMapper::toDTO).toList();
    }


}
