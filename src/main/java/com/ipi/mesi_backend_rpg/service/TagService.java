package com.ipi.mesi_backend_rpg.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.TagRequestDTO;
import com.ipi.mesi_backend_rpg.dto.TagResponseDTO;
import com.ipi.mesi_backend_rpg.mapper.TagMapper;
import com.ipi.mesi_backend_rpg.model.Tag;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.TagRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final ModuleRepository moduleRepository;

    @Transactional
    public TagResponseDTO createTag(TagRequestDTO tagDTO) {
        // Utiliser tagDTO au lieu de requestDTO
        Tag tag = tagMapper.toEntityFromRequest(tagDTO);

        // Sauvegarder l'entité
        Tag savedTag = tagRepository.save(tag);

        // Retourner un TagDTO, pas un TagResponseDTO
        return tagMapper.toResponseDTO(savedTag);
    }

    @Transactional
    public TagResponseDTO createTagFromRequest(TagRequestDTO requestDTO) {
        // Convertir le DTO en entité
        Tag tag = tagMapper.toEntityFromRequest(requestDTO);

        // Sauvegarder l'entité
        Tag savedTag = tagRepository.save(tag);

        // Convertir l'entité sauvegardée en DTO de réponse
        return tagMapper.toResponseDTO(savedTag);
    }

    @Transactional()
    public TagResponseDTO getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));

        return tagMapper.toResponseDTO(tag);
    }

    @Transactional()
    public List<TagResponseDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tagMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TagResponseDTO updateTag(Long id, TagRequestDTO requestDTO) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));

        // Mettre à jour le nom
        tag.setName(requestDTO.name());

        // Mettre à jour les modules si nécessaire
        if (requestDTO.moduleIds() != null) {
            List<com.ipi.mesi_backend_rpg.model.Module> modules = moduleRepository.findAllById(requestDTO.moduleIds());
            tag.setModules(modules);
        }

        Tag updatedTag = tagRepository.save(tag);
        return tagMapper.toResponseDTO(updatedTag);
    }

    @Transactional
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    /**
     * Recherche des tags par nom contenant la chaîne de caractères spécifiée
     * @param query La chaîne de caractères à rechercher dans les noms de tags
     * @return Une liste de TagResponseDTO correspondant aux critères de recherche
     */
    @Transactional()
    public List<TagResponseDTO> searchTagsByName(String query) {
        // Recherche insensible à la casse des tags dont le nom contient la requête
        List<Tag> tags = tagRepository.findByNameContainingIgnoreCase(query);
        
        // Convertir les entités en DTOs de réponse
        return tags.stream()
            .map(tagMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Récupère les tags les plus utilisés, triés par nombre de modules décroissant
     * @return Une liste de TagResponseDTO représentant les tags les plus utilisés
     */
    @Transactional()
    public List<TagResponseDTO> getMostUsedTags() {
        // Récupérer les tags triés par nombre de modules décroissant
        List<Tag> mostUsedTags = tagRepository.findAllOrderByModuleCountDesc();
        
        // Convertir les entités en DTOs de réponse
        return mostUsedTags.stream()
            .map(tagMapper::toResponseDTO)
            .collect(Collectors.toList());
    }
}