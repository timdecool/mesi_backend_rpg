package com.ipi.mesi_backend_rpg.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.dto.PictureDTO;
import com.ipi.mesi_backend_rpg.model.Module;
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

    public Module toEntity(ModuleRequestDTO moduleRequestDTO) {
        Module module = new Module(
                moduleRequestDTO.title(),
                moduleRequestDTO.description(),
                userRepository.findById(moduleRequestDTO.creator().id()).orElseThrow(),
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
