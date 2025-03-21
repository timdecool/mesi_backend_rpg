package com.ipi.mesi_backend_rpg.controller;


import com.ipi.mesi_backend_rpg.dto.TagDTO;
import com.ipi.mesi_backend_rpg.model.Tag;
import com.ipi.mesi_backend_rpg.repository.TagRepository;
import com.ipi.mesi_backend_rpg.service.TagMapperService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    TagRepository tagRepository;
    TagMapperService tagMapperService;

    public TagController(TagRepository tagRepository, TagMapperService tagMapperService) {
        this.tagRepository = tagRepository;
        this.tagMapperService = tagMapperService;
    }

    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagRepository.findAll().stream().map(tagMapperService::toDTO).toList();
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @GetMapping("/{name}")
    public ResponseEntity<TagDTO> getTagByName(@PathVariable String name) {
        
        Tag tag = tagRepository.findByName(name);

        if (tag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        tagMapperService.toDTO(tag);
        return new ResponseEntity<>(tagMapperService.toDTO(tag), HttpStatus.OK);

    }

    @PostMapping
    public ResponseEntity<TagDTO> createTag(@RequestBody TagDTO tagDTO) {
        Tag savedTag = tagRepository.save(tagMapperService.toEntity(tagDTO));
        return new ResponseEntity<>(tagMapperService.toDTO(savedTag), HttpStatus.CREATED);
    }

}
