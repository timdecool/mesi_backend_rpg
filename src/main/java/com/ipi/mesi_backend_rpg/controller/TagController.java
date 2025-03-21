package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.TagDTO;
import com.ipi.mesi_backend_rpg.model.Tag;
import com.ipi.mesi_backend_rpg.repository.TagRepository;
import com.ipi.mesi_backend_rpg.service.TagMapperService;
import jakarta.validation.Valid;
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

    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Integer id) {
        Tag tag = tagRepository.findById(id).orElse(null);

        if (tag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(tagMapperService.toDTO(tag), HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<TagDTO> getTagByName(@PathVariable String name) {

        Tag tag = tagRepository.findByName(name);

        if (tag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        tagMapperService.toDTO(tag);
        return new ResponseEntity<>(tagMapperService.toDTO(tag), HttpStatus.OK);
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<List<TagDTO>> searchTags(@PathVariable String query) {
        List<Tag> tags = tagRepository.findSearchTag(query);

        if (tags.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        List<TagDTO> foundTags = tags.stream().map(tagMapperService::toDTO).toList();
        return new ResponseEntity<>(foundTags, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagDTO tagDTO) {
        Tag savedTag = tagRepository.save(tagMapperService.toEntity(tagDTO));
        return new ResponseEntity<>(tagMapperService.toDTO(savedTag), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TagDTO> deleteTag(@PathVariable Integer id) {

        Tag tag = tagRepository.findById(id).orElse(null);

        if (tag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        tagRepository.delete(tag);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> updateTag(@PathVariable Integer id, @Valid @RequestBody TagDTO tagDTO) {
        Tag tag = tagRepository.findById(id).orElse(null);

        if (tag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!tag.getId().equals(tagDTO.id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        
        Tag updatedTag = tagMapperService.toEntity(tagDTO);
        updatedTag.setId(tag.getId());
        tagRepository.save(updatedTag);

        return new ResponseEntity<>(tagMapperService.toDTO(updatedTag), HttpStatus.OK);
    }


}
