package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.ModuleCommentDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleCommentMapper;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleComment;
import com.ipi.mesi_backend_rpg.model.ModuleVersion;
import com.ipi.mesi_backend_rpg.model.NotificationType;
import com.ipi.mesi_backend_rpg.repository.ModuleCommentRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleVersionRepository;
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
    private final UserService userService;
    private final ModuleRepository moduleRepository;
    private final ModuleVersionRepository moduleVersionRepository;
    private final NotificationService notificationService;

    public ModuleCommentDTO findById(Long id) {
        ModuleComment moduleComment = moduleCommentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "module comment not found"));
        return moduleCommentMapper.toDTO(moduleComment);
    }

    public ModuleCommentDTO createComment(ModuleCommentDTO moduleCommentDTO) {
        ModuleComment moduleComment = new ModuleComment();
        Module module = moduleRepository.findById(moduleCommentDTO.moduleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        moduleComment.setModule(module);
        moduleComment.setModuleVersion(moduleVersionRepository.findById(moduleCommentDTO.moduleVersionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ModuleVersion not found")));
        moduleComment.setComment(moduleCommentDTO.comment());
        moduleComment.setUser(userService.getAuthenticatedUser());
        ModuleComment saved = moduleCommentRepository.save(moduleComment);

        // Notify the module creator
        String content = moduleComment.getUser().getUsername() + " a commenté votre module \"" + module.getTitle() + "\".";
        notificationService.createNotification(
                NotificationType.MODULE_COMMENT,
                content,
                module.getCreator(), // Recipient: the creator of the module
                moduleComment.getUser(), // Sender: the user who commented
                module // Module associated with the comment
        );

        return moduleCommentMapper.toDTO(saved);
    }

    public ModuleCommentDTO updateComment(ModuleCommentDTO moduleCommentDTO, Long id) {
        ModuleComment comment = moduleCommentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "comment not found"));

        if(!comment.getUser().getId().equals(userService.getAuthenticatedUser().getId()))
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have the right to edit this comment.");
        }

        comment.setComment(moduleCommentDTO.comment());
        comment.setUpdatedAt(java.time.LocalDateTime.now());
        ModuleComment savedComment = moduleCommentRepository.save(comment);
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