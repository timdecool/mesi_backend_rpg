package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.TagDTO;
import com.ipi.mesi_backend_rpg.mapper.TagMapper;
import com.ipi.mesi_backend_rpg.model.Tag;
import com.ipi.mesi_backend_rpg.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TagService {

    TagRepository tagRepository;
    TagMapper tagMapper;

    public TagService(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tagMapper::toDTO)
                .toList();
    }

    public TagDTO getTagById(Integer id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));

        return tagMapper.toDTO(tag);
    }

    public TagDTO getTagByName(String name) {
        Tag tag = tagRepository.findByName(name);

        if (tag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found");
        }

        return tagMapper.toDTO(tag);
    }

    public List<TagDTO> searchTags(String query) {
        List<Tag> tags = tagRepository.findSearchTag(query);

        if (tags.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No tags found");
        }

        return tags.stream()
                .map(tagMapper::toDTO)
                .toList();
    }

    public TagDTO createTag(TagDTO tagDTO) {
        Tag tag = tagMapper.toEntity(tagDTO);
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toDTO(savedTag);
    }

    public void deleteTag(Integer id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));

        tagRepository.delete(tag);
    }

    public TagDTO updateTag(Integer id, TagDTO tagDTO) {
        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));

        if (!existingTag.getId().equals(tagDTO.id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and body do not match");
        }

        Tag updatedTag = tagMapper.toEntity(tagDTO);
        updatedTag.setId(existingTag.getId());
        tagRepository.save(updatedTag);
        return tagMapper.toDTO(updatedTag);
    }
}