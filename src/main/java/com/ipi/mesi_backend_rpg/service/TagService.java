package com.ipi.mesi_backend_rpg.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.TagRequestDTO;
import com.ipi.mesi_backend_rpg.dto.TagResponseDTO;
import com.ipi.mesi_backend_rpg.mapper.TagMapper;
import com.ipi.mesi_backend_rpg.model.Tag;
import com.ipi.mesi_backend_rpg.model.Module;
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
        // Créer le nouveau tag
        Tag tag = new Tag();
        tag.setName(tagDTO.name());

        // Sauvegarder le tag
        Tag savedTag = tagRepository.save(tag);

        // Associer aux modules si spécifiés
        if (tagDTO.moduleIds() != null && !tagDTO.moduleIds().isEmpty()) {
            List<Module> modules = moduleRepository.findAllById(tagDTO.moduleIds());

            for (Module module : modules) {
                // Ajouter le tag au module
                if (module.getTags() == null) {
                    module.setTags(new ArrayList<Tag>());
                }
                module.getTags().add(savedTag);

                // Ajouter le module au tag
                if (savedTag.getModules() == null) {
                    savedTag.setModules(new ArrayList<Module>());
                }
                savedTag.getModules().add(module);

                // Sauvegarder le module mis à jour
                moduleRepository.save(module);
            }
        }

        // Récupérer le tag mis à jour
        savedTag = tagRepository.findById(savedTag.getId())
                .orElseThrow(() -> new RuntimeException("Tag not found after save"));

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
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + id));

        // Supprimer d'abord les associations avec les modules
        if (tag.getModules() != null) {
            // Créer une copie de la liste pour éviter ConcurrentModificationException
            List<Module> modules = new ArrayList<Module>(tag.getModules());

            // Supprimer le tag de chaque module
            for (Module module : modules) {
                module.removeTag(tag);
            }

            // Vider la liste des modules associés au tag
            tag.getModules().clear();
        }

        // Maintenant, supprimer le tag
        tagRepository.delete(tag);
    }

    /**
     * Recherche des tags par nom contenant la chaîne de caractères spécifiée
     * 
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
     * 
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

    /**
     * Récupère tous les tags associés à un module spécifique
     * 
     * @param moduleId L'identifiant du module
     * @return Une liste de TagResponseDTO représentant les tags liés au module
     */
    @Transactional()
    public List<TagResponseDTO> getTagsByModuleId(Long moduleId) {
        // Vérifier si le module existe
        if (!moduleRepository.existsById(moduleId)) {
            throw new RuntimeException("Module not found with id: " + moduleId);
        }

        // Récupérer les tags associés au module
        List<Tag> tags = tagRepository.findByModulesId(moduleId);

        // Convertir les entités en DTOs de réponse
        return tags.stream()
                .map(tagMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TagResponseDTO removeModuleFromTag(Long tagId, Long moduleId) {
        // Trouver le tag
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tag not found with id: " + tagId));

        // Trouver le module
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Module not found with id: " + moduleId));

        // Vérifier si le module est associé au tag
        if (tag.getModules() == null || !tag.getModules().contains(module)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Module is not associated with this tag");
        }

        // Supprimer l'association des deux côtés
        tag.getModules().remove(module);
        module.getTags().remove(tag);

        // Sauvegarder les modifications
        moduleRepository.save(module);
        Tag updatedTag = tagRepository.save(tag);

        // Retourner le tag mis à jour
        return tagMapper.toResponseDTO(updatedTag);
    }
}