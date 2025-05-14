package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file) {
        try {
            String fileId = fileStorageService.uploadFile(file

            );
            return ResponseEntity.ok(fileId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/retrieve/{fileId}")
    public ResponseEntity<FileStorageService.FileResponse> retrieveFile(@PathVariable String fileId) {
        try {
            FileStorageService.FileResponse fileResponse = fileStorageService.retrieveFile(fileId);
            return ResponseEntity.ok(fileResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}