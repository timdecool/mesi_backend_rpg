package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseSummaryDTO;
import com.ipi.mesi_backend_rpg.dto.PictureDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ModuleMapper {

    private final ModuleVersionMapper moduleVersionMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final UserRepository userRepository;
    private final ModuleAccessMapper moduleAccessMapper;
    private final PictureMapper pictureMapper;

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
                pictureDTO);
    }

    public ModuleResponseSummaryDTO toSummaryDTO(Module module) {
        PictureDTO pictureDTO = null;
        if (module.getPicture() != null) {
            pictureDTO = pictureMapper.toDTO(module.getPicture());
        }

        return new ModuleResponseSummaryDTO(
                module.getId(),
                module.getTitle(),
                module.getDescription(),
                userMapper.toDTO(module.getCreator()),
                module.getVersions().stream().map(moduleVersionMapper::toDTO).toList(),
                pictureDTO);
    }

    public Module toEntity(ModuleRequestDTO moduleRequestDTO) {
        // Check if creator data is provided
        if (moduleRequestDTO.creator() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator information is required");
        }

        // Check if creator ID is provided
        if (moduleRequestDTO.creator().id() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Creator ID is required");
        }

        // Log the ID we're looking for to help with debugging
        Long creatorId = moduleRequestDTO.creator().id();
        System.out.println("Looking for user with ID: " + creatorId);

        // Try to find the user
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with ID: " + creatorId));

        // Create the module with the found user
        Module module = new Module(
                moduleRequestDTO.title(),
                moduleRequestDTO.description(),
                creator,
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
