package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.ModuleAccessDTO;
import com.ipi.mesi_backend_rpg.model.AccessRight;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.service.ModuleAccessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/module-access")
public class ModuleAccessController {

    ModuleAccessService moduleAccessService;

    public ModuleAccessController(ModuleAccessService moduleAccessService) {
        this.moduleAccessService = moduleAccessService;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ModuleAccessDTO> getModuleAccessById(@PathVariable Integer id) {
        ModuleAccessDTO moduleAccess = moduleAccessService.getModuleAccessById(id);
        return new ResponseEntity<>(moduleAccess, HttpStatus.OK);
    }

    @GetMapping("/module/{module}")
    public ResponseEntity<List<ModuleAccessDTO>> getModuleAccessByModule(@PathVariable Module module) {
        List<ModuleAccessDTO> moduleAccesses = moduleAccessService.getModuleAccessByModule(module);
        return new ResponseEntity<>(moduleAccesses, HttpStatus.OK);
    }

    @GetMapping("/user/{user}/module/{module}")
    public ResponseEntity<ModuleAccessDTO> getModuleAccessByUser(@PathVariable User user, @PathVariable Module module) {
        ModuleAccessDTO moduleAccess = moduleAccessService.getModuleAccessByUser(module, user);
        return new ResponseEntity<>(moduleAccess, HttpStatus.OK);
    }

    @PostMapping("/module/{module-id}")
    public ResponseEntity<ModuleAccessDTO> createModuleAccess(@Valid @RequestBody ModuleAccessDTO moduleAccessDTO, @PathVariable("module-id") Long moduleId) {
        ModuleAccessDTO createdModuleAccess = moduleAccessService.createModuleAccess(moduleAccessDTO, moduleId);

        return new ResponseEntity<>(createdModuleAccess, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleAccessDTO> updateModuleAccess(@PathVariable Integer id, @Valid @RequestBody ModuleAccessDTO moduleAccessDTO) {
        ModuleAccessDTO updatedModuleAccess = moduleAccessService.updateModuleAccess(id, moduleAccessDTO);
        return new ResponseEntity<>(updatedModuleAccess, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ModuleAccessDTO> deleteModuleAccess(@PathVariable Integer id) {
        ModuleAccessDTO deletedModuleAccess = moduleAccessService.deleteModuleAccess(id);
        return new ResponseEntity<>(deletedModuleAccess, HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{id}/rights/{accessRight}")
    public ResponseEntity<ModuleAccessDTO> toggleAccessRight(
            @PathVariable Integer id,
            @PathVariable AccessRight accessRight) {

        ModuleAccessDTO updatedAccess = moduleAccessService.toggleAccessRight(id, accessRight);
        return ResponseEntity.ok(updatedAccess);
    }


}
