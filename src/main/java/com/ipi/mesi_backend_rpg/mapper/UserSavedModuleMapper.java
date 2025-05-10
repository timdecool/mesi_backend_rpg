package com.ipi.mesi_backend_rpg.mapper;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.UserSavedModuleDTO;
import com.ipi.mesi_backend_rpg.model.UserSavedModule;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSavedModuleMapper {

    private final ModuleRepository moduleRepository;

    public UserSavedModuleDTO toDTO(UserSavedModule userSavedModule) {
        return new UserSavedModuleDTO(
                userSavedModule.getSavedModuleId(),
                userSavedModule.getUserId(),
                userSavedModule.getModuleId(),
                userSavedModule.getModuleVersionId(),
                userSavedModule.getFolderId(),
                userSavedModule.getAlias());
    }

    public UserSavedModule toEntity(UserSavedModuleDTO dto) {
        UserSavedModule entity = new UserSavedModule();
        entity.setUserId(dto.userId());
        moduleRepository.findById(dto.moduleId())
            .ifPresent(entity::setModule);
        entity.setModuleVersionId(dto.moduleVersionId());
        entity.setFolderId(dto.folderId());
        entity.setAlias(dto.alias());
        return entity;
    }
}