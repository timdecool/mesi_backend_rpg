package com.ipi.mesi_backend_rpg.controller;

import java.util.List;
import java.util.Map;

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

import com.ipi.mesi_backend_rpg.dto.UserSavedModuleDTO;
import com.ipi.mesi_backend_rpg.service.UserSavedModuleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/saved-modules")
@RequiredArgsConstructor
public class UserSavedModuleController {

    private final UserSavedModuleService userSavedModuleService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserSavedModuleDTO>> getAllModulesByUserId(@PathVariable Long userId) {
        return userSavedModuleService.getAllModulesByUserId(userId);
    }

    @GetMapping("/{savedModuleId}")
    public ResponseEntity<UserSavedModuleDTO> getSavedModuleById(@PathVariable Long savedModuleId) {
        return userSavedModuleService.getSavedModuleById(savedModuleId);
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<UserSavedModuleDTO>> getModulesByFolderId(@PathVariable Long folderId) {
        return userSavedModuleService.getModulesByFolderId(folderId);
    }

    @GetMapping("/user/{userId}/folder/{folderId}")
    public ResponseEntity<List<UserSavedModuleDTO>> getModulesByUserIdAndFolderId(
            @PathVariable Long userId,
            @PathVariable Long folderId) {
        return userSavedModuleService.getModulesByUserIdAndFolderId(userId, folderId);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSavedModuleDTO>> searchModulesByAlias(
            @RequestParam Long userId,
            @RequestParam String alias) {
        return userSavedModuleService.searchModulesByAlias(userId, alias);
    }

    @PostMapping
    public ResponseEntity<UserSavedModuleDTO> createSavedModule(
            @Valid @RequestBody UserSavedModuleDTO userSavedModuleDTO) {
        return userSavedModuleService.createSavedModule(userSavedModuleDTO);
    }

    @PutMapping("/{savedModuleId}")
    public ResponseEntity<UserSavedModuleDTO> updateSavedModule(
            @PathVariable Long savedModuleId,
            @Valid @RequestBody UserSavedModuleDTO userSavedModuleDTO) {
        return userSavedModuleService.updateSavedModule(savedModuleId, userSavedModuleDTO);
    }

    @PatchMapping("/move")
    public ResponseEntity<Map<String, Object>> moveModulesToFolder(
            @RequestBody Map<String, Object> request) {
        return userSavedModuleService.moveModulesToFolder(request);
    }

    @DeleteMapping("/{savedModuleId}")
    public ResponseEntity<Void> deleteSavedModule(@PathVariable Long savedModuleId) {
        return userSavedModuleService.deleteSavedModule(savedModuleId);
    }

    @DeleteMapping("/folder/{folderId}")
    public ResponseEntity<Map<String, Object>> deleteModulesByFolderId(@PathVariable Long folderId) {
        return userSavedModuleService.deleteModulesByFolderId(folderId);
    }
}