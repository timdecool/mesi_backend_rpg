package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    public List<Module> findAll() {
        return moduleRepository.findAll();
    }

    public void findById(Long id) {
        return moduleRepository.findById(id).ifPresent(module -> {

        });
    }
}
