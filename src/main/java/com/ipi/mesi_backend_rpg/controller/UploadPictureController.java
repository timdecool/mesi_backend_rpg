package com.ipi.mesi_backend_rpg.controller;


import com.ipi.mesi_backend_rpg.service.UploadPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upload-picture")
public class UploadPictureController {

    private final UploadPictureService uploadPictureService;

    @PostMapping
    public String upload(@RequestParam("file") MultipartFile multipartFile) {
        return uploadPictureService.upload(multipartFile);
    }
}
