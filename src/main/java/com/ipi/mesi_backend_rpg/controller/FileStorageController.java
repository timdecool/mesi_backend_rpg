package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.FileMetaDataDTO;
import com.ipi.mesi_backend_rpg.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam MultipartFile file) throws IOException {
        String fileId = fileStorageService.uploadFile(file);
        return new ResponseEntity<>(fileId, HttpStatus.CREATED);
    }

    @GetMapping("/retrieve/{fileId}")
    public ResponseEntity<FileMetaDataDTO> retrieveFile(@PathVariable String fileId) throws IOException {

        FileMetaDataDTO file = fileStorageService.retrieveFile(fileId);
        return new ResponseEntity<>(file, HttpStatus.OK);

    }
}