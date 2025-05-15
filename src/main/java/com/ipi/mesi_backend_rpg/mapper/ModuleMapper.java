package com.ipi.mesi_backend_rpg.mapper;

import java.time.LocalDateTime;

import com.ipi.mesi_backend_rpg.service.ModuleRatingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.dto.PictureDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuleMapper {

    private final ModuleVersionMapper moduleVersionMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final UserRepository userRepository;
    private final ModuleAccessMapper moduleAccessMapper;
    private final PictureMapper pictureMapper;
    private final ModuleRatingService moduleRatingService;

    public ModuleResponseDTO toDTO(Module module) {
        PictureDTO pictureDTO = null;
        if (module.getPicture() != null) {
            pictureDTO = pictureMapper.toDTO(module.getPicture());
        }

        return new ModuleResponseDTO(
                module.getId(),
                module.getTitle(),
                module.getDescription(),
                module.getIsTemplate(),
                module.getType(),
                userMapper.toDTO(module.getCreator()),
                module.getCreatedAt(),
                module.getUpdatedAt(),
                module.getVersions().stream().map(moduleVersionMapper::toDTO).toList(),
                module.getAccesses().stream().map(moduleAccessMapper::toDTO).toList(),
                module.getTags().stream().map(tagMapper::toDTO).toList(),
                pictureDTO,
                moduleRatingService.findAggregatedRatingsByModule(module)
                );
    }

    public Module toEntity(ModuleRequestDTO moduleRequestDTO) {

        // Create the module with the found user
        Module module = new Module(
                moduleRequestDTO.title(),
                moduleRequestDTO.description(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                moduleRequestDTO.isTemplate(),
                moduleRequestDTO.type());

        if (moduleRequestDTO.picture() != null) {
            module.setPicture(pictureMapper.toEntity(moduleRequestDTO.picture()));
        }

        return module;
    }
}
