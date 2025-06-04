package com.ipi.mesi_backend_rpg.mapper;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.ModuleCommentDTO;
import com.ipi.mesi_backend_rpg.model.ModuleComment;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModuleCommentMapper {

    private final UserMapper userMapper;
    private final ModuleRepository moduleRepository;
    private final ModuleVersionRepository moduleVersionRepository;

    public ModuleCommentDTO toDTO(ModuleComment moduleComment) {
        return new ModuleCommentDTO(
                moduleComment.getId(),
                moduleComment.getModule().getId(),
                moduleComment.getModuleVersion().getId(),
                userMapper.toDTO(moduleComment.getUser()),
                moduleComment.getComment(),
                moduleComment.getCreatedAt(),
                moduleComment.getUpdatedAt()
        );
    }

    public ModuleComment toEntity(ModuleCommentDTO moduleCommentDTO) {
        ModuleComment moduleComment = new ModuleComment();
        moduleComment.setId(moduleCommentDTO.id());
        moduleComment.setModule(moduleRepository.findById(moduleCommentDTO.moduleId())
                .orElseThrow(() -> new IllegalArgumentException("Module not found")));
        moduleComment.setModuleVersion(moduleVersionRepository.findById(moduleCommentDTO.moduleVersionId())
                .orElseThrow(() -> new IllegalArgumentException("ModuleVersion not found")));
        moduleComment.setComment(moduleCommentDTO.comment());
        return moduleComment;
    }
}
