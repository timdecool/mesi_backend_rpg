package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.ModuleCommentDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.service.ModuleCommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class ModuleCommentController {

    private final ModuleCommentService moduleCommentService;
    public ModuleCommentController(ModuleCommentService moduleCommentService) {
        this.moduleCommentService = moduleCommentService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleCommentDTO> getModuleCommentById(@PathVariable("id") Long id) {
        ModuleCommentDTO moduleComment = moduleCommentService.findById(id);
        return new ResponseEntity<>(moduleComment, HttpStatus.OK);
    }


    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<ModuleCommentDTO>> getModuleCommentsByModule(@PathVariable("moduleId") Module module) {
        List<ModuleCommentDTO> moduleVersions = moduleCommentService.findAllByModule(module);
        return new ResponseEntity<>(moduleVersions, HttpStatus.OK);
    }

    @GetMapping("/version/{moduleVersionId}")
    public ResponseEntity<List<ModuleCommentDTO>> getModuleCommentsByModuleVersion(@PathVariable("moduleVersionId") ModuleVersion moduleVersion) {
        List<ModuleCommentDTO> moduleVersions = moduleCommentService.findAllByModuleVersion(moduleVersion);
        return new ResponseEntity<>(moduleVersions, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ModuleCommentDTO> createModuleComment(
            @RequestBody ModuleCommentDTO moduleCommentDTO) {
        ModuleCommentDTO moduleComment = moduleCommentService.createComment(moduleCommentDTO);
        return new ResponseEntity<>(moduleComment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleCommentDTO> updateModuleComment(
            @PathVariable("id") Long id,
            @Valid @RequestBody ModuleCommentDTO moduleCommentDTO
    ) {
        ModuleCommentDTO moduleComment = moduleCommentService.updateComment(moduleCommentDTO, id);
        return new ResponseEntity<>(moduleComment, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModuleVersion(@PathVariable("id") Long id) {
        moduleCommentService.deleteComment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
