package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.ModuleRequestDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleResponseDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ModuleMapper {

    private final ModuleVersionMapper moduleVersionMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final UserRepository userRepository;
    private final ModuleAccessMapper moduleAccessMapper;

    public ModuleResponseDTO toDTO(Module module) {
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
                module.getTags().stream().map(tagMapper::toDTO).toList()
        );
    }

    public Module toEntity(ModuleRequestDTO moduleRequestDTO) {
        return new Module(
                moduleRequestDTO.title(),
                moduleRequestDTO.description(),
                userRepository.findById(moduleRequestDTO.creatorId()).orElseThrow(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                moduleRequestDTO.isTemplate(),
                moduleRequestDTO.type()
        );
    }
}
