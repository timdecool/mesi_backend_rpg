package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.service.ModuleMapperService;
import com.ipi.mesi_backend_rpg.service.ModuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService moduleService;
    private final ModuleMapperService moduleMapperService;
    public ModuleController(ModuleService moduleService, ModuleMapperService moduleMapperService) {
        this.moduleService = moduleService;
        this.moduleMapperService = moduleMapperService;
    }

    @GetMapping
    public ResponseEntity<List<ModuleResponseDTO>> getAllModules() {
        List<ModuleResponseDTO> modules = moduleService.findAll()
                .stream().map(moduleMapperService::toDTO).toList();
        return new ResponseEntity<>(modules, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponseDTO> getModuleById(@PathVariable(name="id", required=false) Long id) {
        moduleService.findById(id);
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }
        return new ResponseEntity<>(moduleMapperService.toDTO(module), HttpStatus.OK);
    }
}
