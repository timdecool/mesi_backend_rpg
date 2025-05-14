package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.AggregatedRatingsDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleCommentDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleRatingDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.service.ModuleCommentService;
import com.ipi.mesi_backend_rpg.service.ModuleRatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class ModuleRatingController {

    private final ModuleCommentService moduleCommentService;
    private final ModuleRatingService moduleRatingService;

    public ModuleRatingController(ModuleCommentService moduleCommentService, ModuleRatingService moduleRatingService) {
        this.moduleCommentService = moduleCommentService;
        this.moduleRatingService = moduleRatingService;
    }


    @GetMapping("/module/{moduleId}")
    public ResponseEntity<AggregatedRatingsDTO> getModuleRatingsByModule(
            @PathVariable("moduleId") Module module
    )
    {
        AggregatedRatingsDTO ratings = moduleRatingService.findAggregatedRatingsByModule(module);
        return new ResponseEntity<>(ratings, HttpStatus.OK);
    }

    @GetMapping("/version/{moduleVersionId}")
    public ResponseEntity<AggregatedRatingsDTO> getModuleRatingsByModuleVersion(
        @PathVariable("moduleVersionId") ModuleVersion moduleVersion
        )
    {
        AggregatedRatingsDTO moduleVersionRatings = moduleRatingService.findAggregatedRatingsByModuleVersion(moduleVersion);
        return new ResponseEntity<>(moduleVersionRatings, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AggregatedRatingsDTO> createModuleRating(
            @RequestBody ModuleRatingDTO moduleRatingDTO) {
        AggregatedRatingsDTO moduleRating = moduleRatingService.createRating(moduleRatingDTO);
        return new ResponseEntity<>(moduleRating, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AggregatedRatingsDTO> updateModuleComment(
            @PathVariable("id") Long id,
            @Valid @RequestBody ModuleRatingDTO moduleRatingDTO
    ) {
        AggregatedRatingsDTO moduleRating = moduleRatingService.updateRating(moduleRatingDTO, id);
        return new ResponseEntity<>(moduleRating, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModuleRating(@PathVariable("id") Long id) {
        moduleRatingService.deleteRating(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
