package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.ModuleAccessDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.service.ModuleAccessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/moduleAccess")
public class ModuleAccessController {

    ModuleAccessService moduleAccessService;

    public ModuleAccessController(ModuleAccessService moduleAccessService) {
        this.moduleAccessService = moduleAccessService;
    }

    @GetMapping
    public ResponseEntity<List<ModuleAccessDTO>> getAllModuleAccess() {
        List<ModuleAccessDTO> moduleAccessList = moduleAccessService.getAllModuleAccesses();
        return new ResponseEntity<>(moduleAccessList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleAccessDTO> getModuleAccessById(@PathVariable Integer id) {
        ModuleAccessDTO moduleAccess = moduleAccessService.getModuleAccessById(id);
        return new ResponseEntity<>(moduleAccess, HttpStatus.OK);
    }

    @GetMapping("/module/{module}")
    public ResponseEntity<ModuleAccessDTO> getModuleAccessByModule(@PathVariable Module module) {
        ModuleAccessDTO moduleAccess = moduleAccessService.getModuleAccessByModule(module);
        return new ResponseEntity<>(moduleAccess, HttpStatus.OK);
    }

    @GetMapping("/user/{user}")
    public ResponseEntity<List<ModuleAccessDTO>> getModuleAccessByUser(@PathVariable User user) {
        List<ModuleAccessDTO> moduleAccess = moduleAccessService.getAllModuleAccessByUser(user);
        return new ResponseEntity<>(moduleAccess, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ModuleAccessDTO> createModuleAccess(@Valid @RequestBody ModuleAccessDTO moduleAccessDTO) {
        ModuleAccessDTO createdModuleAccess = moduleAccessService.createModuleAccess(moduleAccessDTO);
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


}
