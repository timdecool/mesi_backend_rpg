package com.ipi.mesi_backend_rpg.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.UserFolderDTO;
import com.ipi.mesi_backend_rpg.service.UserFolderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/user/folders")
@RequiredArgsConstructor
public class UserFolderController {

    private final UserFolderService userFolderService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserFolderDTO>> getAllFoldersByUserId(@PathVariable Long userId) {
        List<UserFolderDTO> folders = userFolderService.getAllFoldersByUserId(userId);
        return new ResponseEntity<>(folders, HttpStatus.OK);
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<UserFolderDTO> getFolderById(@PathVariable Long folderId) {
        return userFolderService.getFolderById(folderId)
                .map(folder -> new ResponseEntity<>(folder, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
    }

    @GetMapping("/parent/{parentFolderId}")
    public ResponseEntity<List<UserFolderDTO>> getChildFolders(@PathVariable Long parentFolderId) {
        List<UserFolderDTO> childFolders = userFolderService.getChildFolders(parentFolderId);
        return new ResponseEntity<>(childFolders, HttpStatus.OK);
    }

    @GetMapping("/root/user/{userId}")
    public ResponseEntity<List<UserFolderDTO>> getRootFolders(@PathVariable Long userId) {
        List<UserFolderDTO> rootFolders = userFolderService.getRootFolders(userId);
        return new ResponseEntity<>(rootFolders, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserFolderDTO>> searchFoldersByName(
            @RequestParam Long userId,
            @RequestParam String name) {
        List<UserFolderDTO> folders = userFolderService.searchFoldersByName(userId, name);
        return new ResponseEntity<>(folders, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserFolderDTO> createFolder(@Valid @RequestBody UserFolderDTO userFolderDTO) {
        UserFolderDTO createdFolder = userFolderService.createFolder(userFolderDTO);
        return new ResponseEntity<>(createdFolder, HttpStatus.CREATED);
    }

    @PutMapping("/{folderId}")
    public ResponseEntity<UserFolderDTO> updateFolder(
            @PathVariable Long folderId,
            @Valid @RequestBody UserFolderDTO userFolderDTO) {
        return userFolderService.updateFolder(folderId, userFolderDTO)
                .map(folder -> new ResponseEntity<>(folder, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        boolean deleted = userFolderService.deleteFolder(folderId);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found");
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
