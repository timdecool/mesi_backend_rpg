package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.ModuleCommentDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleVersionDTO;
import com.ipi.mesi_backend_rpg.mapper.BlockMapper;
import com.ipi.mesi_backend_rpg.mapper.ModuleCommentMapper;
import com.ipi.mesi_backend_rpg.mapper.ModuleVersionMapper;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleComment;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.ModuleCommentRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModuleCommentService {

    private final ModuleCommentRepository moduleCommentRepository;
    private final ModuleCommentMapper moduleCommentMapper;

    public ModuleCommentDTO findById(Long id) {
        ModuleComment moduleComment = moduleCommentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module comment not found"));
        return moduleCommentMapper.toDTO(moduleComment);
    }

    public ModuleCommentDTO createComment(ModuleCommentDTO moduleCommentDTO) {
        ModuleComment moduleComment = moduleCommentMapper.toEntity(moduleCommentDTO);
        ModuleComment saved = moduleCommentRepository.save(moduleComment);
        return moduleCommentMapper.toDTO(saved);
    }

    public ModuleCommentDTO updateComment(ModuleCommentDTO moduleCommentDTO, Long id) {
        ModuleComment comment = moduleCommentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "comment not found"));
        ModuleComment newComment = moduleCommentMapper.toEntity(moduleCommentDTO);

        newComment.setId(comment.getId());
        newComment.setCreatedAt(comment.getCreatedAt());
        newComment.setModule(comment.getModule());
        newComment.setModuleVersion(comment.getModuleVersion());
        ModuleComment savedComment = moduleCommentRepository.save(newComment);
        return moduleCommentMapper.toDTO(savedComment);
    }

    public void deleteComment(Long id) {
        ModuleComment comment = moduleCommentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found"));

        moduleCommentRepository.delete(comment);
    }

    public List<ModuleCommentDTO> findAllByModule(Module module) {
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "module not found");
        }

        return moduleCommentRepository.findAllByModule(module).stream().map(moduleCommentMapper::toDTO).toList();
    }

    public List<ModuleCommentDTO> findAllByModuleVersion(ModuleVersion moduleVersion) {
        if (moduleVersion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found");
        }

        return moduleCommentRepository.findAllByModuleVersion(moduleVersion).stream().map(moduleCommentMapper::toDTO).toList();
    }

}
