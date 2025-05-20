package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.ModuleCommentDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleCommentMapper;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleComment;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.repository.ModuleCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

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

    public List<ModuleCommentDTO> findAllByModule(Module module, int page, int limit) {
        if (module == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "module not found");
        }

        return moduleCommentRepository.findAllByModule(module, PageRequest.of(page, limit)).stream().map(moduleCommentMapper::toDTO).toList();
    }

    public List<ModuleCommentDTO> findAllByModuleVersion(ModuleVersion moduleVersion, int page, int limit) {
        if (moduleVersion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "module version not found");
        }

        return moduleCommentRepository.findAllByModuleVersion(moduleVersion, PageRequest.of(page, limit)).stream().map(moduleCommentMapper::toDTO).toList();
    }

}
