package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
public class ModuleController {

    private final ModuleService moduleService;
    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    public ResponseEntity<List<ModuleResponseDTO>> getAllModules() {
        List<ModuleResponseDTO> modules = moduleService.findAllModules();
        return new ResponseEntity<>(modules, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleResponseDTO> getModuleById(@PathVariable(name="id", required=false) Long id) {
        ModuleResponseDTO module = moduleService.findById(id);
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }
        return new ResponseEntity<>(module, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ModuleResponseDTO> createModule(@Valid @RequestBody ModuleRequestDTO moduleRequestDTO) {
        ModuleResponseDTO module = moduleService.createModule(moduleRequestDTO);
        return new ResponseEntity<>(module, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleResponseDTO> updateModule(
            @Valid @RequestBody ModuleRequestDTO moduleRequestDTO,
            @PathVariable Long id
    ) {
        ModuleResponseDTO module = moduleService.updateModule(id, moduleRequestDTO);
        return new ResponseEntity<>(module, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
