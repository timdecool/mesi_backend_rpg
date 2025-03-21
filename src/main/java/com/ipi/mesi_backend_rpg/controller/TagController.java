package com.ipi.mesi_backend_rpg.controller;


import com.ipi.mesi_backend_rpg.dto.TagDTO;
import com.ipi.mesi_backend_rpg.model.Tag;
import com.ipi.mesi_backend_rpg.repository.TagRepository;
import com.ipi.mesi_backend_rpg.service.TagMapperService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    TagRepository tagRepository;
    TagMapperService tagMapperService;

    public TagController(TagRepository tagRepository, TagMapperService tagMapperService) {
        this.tagRepository = tagRepository;
        this.tagMapperService = tagMapperService;
    }

    @GetMapping
    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream().map(tagMapperService::mapTagToTagDTO).collect(Collectors.toList());
    }

    @GetMapping("/{name}")
    public TagDTO getTagByName(@PathVariable String name) {
        Tag tag = tagRepository.findByName(name);
        if (tag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

    return tagMapperService.mapTagToTagDTO(tag);

    }


    @PostMapping
    public ResponseEntity<TagDTO> createTag(@RequestBody TagDTO tagDTO) {
        Tag tag = new Tag();
        tag.setName(tagDTO.name());
        tag.setModules(new ArrayList<>());
        Tag savedTag = tagRepository.save(tag);

        return new ResponseEntity<>(tagMapperService.mapTagToTagDTO(savedTag), HttpStatus.CREATED);
    }

}
