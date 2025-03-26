package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.service.ModuleVersionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
public class ModuleVersionController {

    private final ModuleVersionService moduleVersionService;
    public ModuleVersionController(ModuleVersionService moduleVersionService) {
        this.moduleVersionService = moduleVersionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleVersionDTO> getModuleVersionById(@PathVariable("id") Long id) {
        ModuleVersionDTO moduleVersion = moduleVersionService.findById(id);
        return new ResponseEntity<>(moduleVersion, HttpStatus.OK);
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<ModuleVersionDTO>> getModuleVersionByModuleId(@PathVariable("moduleId") Module module) {
        List<ModuleVersionDTO> moduleVersions = moduleVersionService.findAllByModule(module);
        return new ResponseEntity<>(moduleVersions, HttpStatus.OK);
    }

    @PostMapping("/module/{moduleId}")
    public ResponseEntity<ModuleVersionDTO> createModuleVersion(
            @PathVariable("moduleId") Module module,
            @RequestBody ModuleVersionDTO moduleVersionDTO) {
        if(module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found");
        }
        ModuleVersionDTO moduleVersion = moduleVersionService.createVersion(module, moduleVersionDTO);
        return new ResponseEntity<>(moduleVersion, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleVersionDTO> updateModuleVersion(
            @PathVariable("id") Long id,
            @Valid @RequestBody ModuleVersionDTO moduleVersionDTO
    ) {
        ModuleVersionDTO moduleVersion = moduleVersionService.updateVersion(moduleVersionDTO, id);
        return new ResponseEntity<>(moduleVersion, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModuleVersion(@PathVariable("id") Long id) {
        moduleVersionService.deleteVersion(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
