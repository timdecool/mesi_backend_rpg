package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.TagDTO;
import com.ipi.mesi_backend_rpg.dto.TagRequestDTO;
import com.ipi.mesi_backend_rpg.dto.TagResponseDTO;
import com.ipi.mesi_backend_rpg.dto.simpleDTO.SimpleModuleDTO;
import com.ipi.mesi_backend_rpg.model.Tag;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import com.ipi.mesi_backend_rpg.model.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagMapper {
    private final ModuleRepository moduleRepository;
    
    public TagDTO toDTO(Tag tag) {
        return new TagDTO(
            tag.getId(),
            tag.getName(),
            tag.getModules()
        );
    }
    
    public Tag toEntity(TagDTO tagDTO) {
        Tag tag = new Tag();
        tag.setName(tagDTO.name());
        
        if (tagDTO.modules() != null && !tagDTO.modules().isEmpty()) {
            List<Module> managedModules = new ArrayList<>();
            for (Module moduleFromDto : tagDTO.modules()) {
                // Récupérer les modules gérés depuis la base de données
                Module managedModule = moduleRepository.findById(moduleFromDto.getId());
                // Vérifier si le module existe avant de l'ajouter
                if (managedModule != null) {
                    managedModules.add(managedModule);
                }
            }
            tag.setModules(managedModules);
        }
        return tag;
    }
    
    public Tag toEntityFromRequest(TagRequestDTO dto) {
        Tag tag = new Tag();
        tag.setName(dto.name());
        
        if (dto.moduleIds() != null && !dto.moduleIds().isEmpty()) {
            // Utilisez le bon type pour l'entité Module
            List<Module> modules = moduleRepository.findAllById(dto.moduleIds());
            tag.setModules(modules);
        }
        return tag;
    }
    
    public TagResponseDTO toResponseDTO(Tag tag) {
        List<SimpleModuleDTO> moduleDTOs = tag.getModules() != null
            ? tag.getModules().stream()
                .map(this::toSimpleModuleDTO)
                .collect(Collectors.toList())
            : List.of();
            
        return new TagResponseDTO(
            tag.getId(),
            tag.getName(),
            moduleDTOs
        );
    }
    
    // Méthode utilitaire pour convertir un Module en SimpleModuleDTO
    private SimpleModuleDTO toSimpleModuleDTO(Module module) {
        return new SimpleModuleDTO(
            module.getId(),
            module.getTitle(),
            module.getDescription()
        );
    }
}