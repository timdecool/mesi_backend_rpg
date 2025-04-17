package com.ipi.mesi_backend_rpg.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.UserSavedModuleDTO;
import com.ipi.mesi_backend_rpg.service.UserSavedModuleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user/saved-modules")
public class UserSavedModuleController {
    private final UserSavedModuleService userSavedModuleService;

    public UserSavedModuleController(UserSavedModuleService userSavedModuleService) {
        this.userSavedModuleService = userSavedModuleService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserSavedModuleDTO>> getAllModulesByUserId(@PathVariable Long userId) {
        List<UserSavedModuleDTO> savedModules = userSavedModuleService.getAllModulesByUserId(userId);
        return new ResponseEntity<>(savedModules, HttpStatus.OK);
    }

    @GetMapping("/{savedModuleId}")
    public ResponseEntity<UserSavedModuleDTO> getSavedModuleById(@PathVariable Long savedModuleId) {
        return userSavedModuleService.getSavedModuleById(savedModuleId)
                .map(module -> new ResponseEntity<>(module, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved module not found"));
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<UserSavedModuleDTO>> getModulesByFolderId(@PathVariable Long folderId) {
        List<UserSavedModuleDTO> savedModules = userSavedModuleService.getModulesByFolderId(folderId);
        return new ResponseEntity<>(savedModules, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/folder/{folderId}")
    public ResponseEntity<List<UserSavedModuleDTO>> getModulesByUserIdAndFolderId(
            @PathVariable Long userId,
            @PathVariable Long folderId) {
        List<UserSavedModuleDTO> savedModules = userSavedModuleService.getModulesByUserIdAndFolderId(userId, folderId);
        return new ResponseEntity<>(savedModules, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSavedModuleDTO>> searchModulesByAlias(
            @RequestParam Long userId,
            @RequestParam String alias) {
        List<UserSavedModuleDTO> savedModules = userSavedModuleService.searchModulesByAlias(userId, alias);
        return new ResponseEntity<>(savedModules, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserSavedModuleDTO> createSavedModule(
            @Valid @RequestBody UserSavedModuleDTO userSavedModuleDTO) {
        UserSavedModuleDTO createdModule = userSavedModuleService.createSavedModule(userSavedModuleDTO);
        return new ResponseEntity<>(createdModule, HttpStatus.CREATED);
    }

    @PutMapping("/{savedModuleId}")
    public ResponseEntity<UserSavedModuleDTO> updateSavedModule(
            @PathVariable Long savedModuleId,
            @Valid @RequestBody UserSavedModuleDTO userSavedModuleDTO) {
        return userSavedModuleService.updateSavedModule(savedModuleId, userSavedModuleDTO)
                .map(module -> new ResponseEntity<>(module, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved module not found"));
    }

    @PatchMapping("/move")
    public ResponseEntity<Map<String, Object>> moveModulesToFolder(
            @RequestBody Map<String, Object> request) {

        @SuppressWarnings("unchecked")
        List<Long> savedModuleIds = (List<Long>) request.get("savedModuleIds");
        Long targetFolderId = Long.valueOf(request.get("targetFolderId").toString());

        boolean moved = userSavedModuleService.moveModulesToFolder(savedModuleIds, targetFolderId);

        if (!moved) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more modules not found");
        }

        Map<String, Object> response = Map.of(
                "success", true,
                "message", savedModuleIds.size() + " modules moved to folder " + targetFolderId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{savedModuleId}")
    public ResponseEntity<Void> deleteSavedModule(@PathVariable Long savedModuleId) {
        boolean deleted = userSavedModuleService.deleteSavedModule(savedModuleId);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved module not found");
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/folder/{folderId}")
    public ResponseEntity<Map<String, Object>> deleteModulesByFolderId(@PathVariable Long folderId) {
        long count = userSavedModuleService.deleteModulesByFolderId(folderId);

        Map<String, Object> response = Map.of(
                "deleted", count,
                "message", count + " saved modules deleted from folder " + folderId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
