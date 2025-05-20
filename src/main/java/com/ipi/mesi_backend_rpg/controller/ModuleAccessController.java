package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.ModuleAccessDTO;
import com.ipi.mesi_backend_rpg.model.AccessRight;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.service.ModuleAccessService;
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

    @PostMapping("/module/{module-id}/user/{user-id}/currentUser/{currentUser-id}")
    public ResponseEntity<ModuleAccessDTO> createModuleAccess(@PathVariable("module-id") Long moduleId,
            @PathVariable("user-id") Long userId, @PathVariable("currentUser-id") Long currentUserId) {
        ModuleAccessDTO createdModuleAccess = moduleAccessService.createModuleAccess(moduleId, userId, currentUserId);

        return new ResponseEntity<>(createdModuleAccess, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/currentUser/{user-id}")
    public ResponseEntity<ModuleAccessDTO> deleteModuleAccess(@PathVariable Integer id,
            @PathVariable("user-id") Long userId) {
        ModuleAccessDTO deletedModuleAccess = moduleAccessService.deleteModuleAccess(id, userId);
        return new ResponseEntity<>(deletedModuleAccess, HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{id}/rights/{accessRight}/currentUser/{user-id}")
    public ResponseEntity<ModuleAccessDTO> toggleAccessRight(
            @PathVariable Integer id,
            @PathVariable AccessRight accessRight,
            @PathVariable("user-id") Long userId) {

        ModuleAccessDTO updatedAccess = moduleAccessService.toggleAccessRight(id, accessRight, userId);
        return ResponseEntity.ok(updatedAccess);
    }

}
