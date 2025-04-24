package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.PictureDTO;
import com.ipi.mesi_backend_rpg.service.PictureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pictures")
public class PictureController {

    private final PictureService pictureService;

    public PictureController(PictureService pictureService) {
        this.pictureService = pictureService;
    }

    @PostMapping("/module/{moduleId}")
    public PictureDTO createPictureForModule(@RequestBody PictureDTO dto, @PathVariable Long moduleId) {
        return pictureService.createModulePicture(dto, moduleId);
    }

    // TODO: Add endpoint for other picture entity creation

//    @GetMapping("/usage-type/{usageType}/usage-id/{usageId}")
//    public ResponseEntity<List<PictureDTO>> getPicturesByUsage() {
//        List<PictureDTO> pictures = pictureService.getPictures(usageId, usageType);
//        return new ResponseEntity<>(pictures, HttpStatus.OK);
//    }

    @PutMapping("/{id}")
    public ResponseEntity<PictureDTO> updatePicture(@RequestBody PictureDTO dto, @PathVariable Long id) {
        PictureDTO picture = pictureService.updatePicture(dto, id);
        return new ResponseEntity<>(picture, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PictureDTO> deletePicture(@PathVariable Long id) {
        PictureDTO picture = pictureService.deletePicture(id);
        return new ResponseEntity<>(picture, HttpStatus.NO_CONTENT);
    }

}
