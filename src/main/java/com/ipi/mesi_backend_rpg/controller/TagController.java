package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.TagRequestDTO;
import com.ipi.mesi_backend_rpg.dto.TagResponseDTO;
import com.ipi.mesi_backend_rpg.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponseDTO> createTag(@Valid @RequestBody TagRequestDTO requestDTO) {
        TagResponseDTO responseDTO = tagService.createTag(requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<List<TagResponseDTO>> searchTags(@PathVariable String query) {
        List<TagResponseDTO> responseDTOs = tagService.searchTagsByName(query);
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("most-used")
    public ResponseEntity<List<TagResponseDTO>> getMostUsedTags() {
        List<TagResponseDTO> responseDTOs = tagService.getMostUsedTags();
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDTO> getTagById(@PathVariable Long id) {
        TagResponseDTO responseDTO = tagService.getTagById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<TagResponseDTO>> getAllTags() {
        List<TagResponseDTO> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDTO> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequestDTO requestDTO) {
        TagResponseDTO responseDTO = tagService.updateTag(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère tous les tags associés à un module spécifique
     * 
     * @param moduleId L'identifiant du module
     * @return Une liste de TagResponseDTO représentant les tags liés au module
     */
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<TagResponseDTO>> getTagsByModuleId(@PathVariable Long moduleId) {
        List<TagResponseDTO> responseDTOs = tagService.getTagsByModuleId(moduleId);
        return ResponseEntity.ok(responseDTOs);
    }

    @DeleteMapping("/{tagId}/modules/{moduleId}")
    public ResponseEntity<TagResponseDTO> removeModuleFromTag(
            @PathVariable Long tagId,
            @PathVariable Long moduleId) {
        TagResponseDTO updatedTag = tagService.removeModuleFromTag(tagId, moduleId);
        return ResponseEntity.ok(updatedTag);
    }
}
