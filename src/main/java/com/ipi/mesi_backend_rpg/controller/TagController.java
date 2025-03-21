package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.TagDTO;
import com.ipi.mesi_backend_rpg.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagService.getAllTags();
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Integer id) {
        TagDTO tag = tagService.getTagById(id);
        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<TagDTO> getTagByName(@PathVariable String name) {
        TagDTO tag = tagService.getTagByName(name);
        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<List<TagDTO>> searchTags(@PathVariable String query) {
        List<TagDTO> tags = tagService.searchTags(query);
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagDTO tagDTO) {
        TagDTO tag = tagService.createTag(tagDTO);
        return new ResponseEntity<>(tag, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Integer id) {
        tagService.deleteTag(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> updateTag(@PathVariable Integer id, @Valid @RequestBody TagDTO tagDTO) {
        TagDTO updatedTag = tagService.updateTag(id, tagDTO);
        return new ResponseEntity<>(updatedTag, HttpStatus.OK);
    }
    
}
