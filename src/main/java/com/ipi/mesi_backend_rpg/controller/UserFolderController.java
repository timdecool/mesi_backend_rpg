package com.ipi.mesi_backend_rpg.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ipi.mesi_backend_rpg.dto.UserFolderDTO;
import com.ipi.mesi_backend_rpg.service.UserFolderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/folders")
@RequiredArgsConstructor
public class UserFolderController {

    private final UserFolderService userFolderService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserFolderDTO>> getAllFoldersByUserId(@PathVariable Long userId) {
        return userFolderService.getAllFoldersByUserId(userId);
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<UserFolderDTO> getFolderById(@PathVariable Long folderId) {
        return userFolderService.getFolderById(folderId);
    }

    @GetMapping("/parent/{parentFolderId}")
    public ResponseEntity<List<UserFolderDTO>> getChildFolders(@PathVariable Long parentFolderId) {
        return userFolderService.getChildFolders(parentFolderId);
    }

    @GetMapping("/root/user/{userId}")
    public ResponseEntity<List<UserFolderDTO>> getRootFolders(@PathVariable Long userId) {
        return userFolderService.getRootFolders(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserFolderDTO>> searchFoldersByName(
            @RequestParam Long userId,
            @RequestParam String name) {
        return userFolderService.searchFoldersByName(userId, name);
    }

    @PostMapping
    public ResponseEntity<UserFolderDTO> createFolder(@Valid @RequestBody UserFolderDTO userFolderDTO) {
        return userFolderService.createFolder(userFolderDTO);
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<UserFolderDTO> updateFolder(
            @PathVariable Long folderId,
            @Valid @RequestBody UserFolderDTO userFolderDTO) {
        return userFolderService.updateFolder(folderId, userFolderDTO);
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        return userFolderService.deleteFolder(folderId);
    }
}