package com.ipi.mesi_backend_rpg.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.service.ModuleService;

import jakarta.validation.Valid;

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

    @GetMapping("/most-saved")
    public ResponseEntity<List<ModuleResponseDTO>> getMostSavedModules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return new ResponseEntity<>(moduleService.getMostSavedModules(page, limit), HttpStatus.OK);
    }

    @GetMapping("/most-recent")
    public ResponseEntity<List<ModuleResponseDTO>> getMostRecentModules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return new ResponseEntity<>(moduleService.getMostRecentModules(page, limit), HttpStatus.OK);
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

    @GetMapping("/search/{query}")
    public ResponseEntity<List<ModuleResponseDTO>> searchModules(@PathVariable String query) {
        List<ModuleResponseDTO> modules = moduleService.searchModules(query);
        return new ResponseEntity<>(modules, HttpStatus.OK);
    }
}
