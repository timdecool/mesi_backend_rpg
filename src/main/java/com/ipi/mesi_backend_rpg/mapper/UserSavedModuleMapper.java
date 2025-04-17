package com.ipi.mesi_backend_rpg.mapper;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.UserSavedModuleDTO;
import com.ipi.mesi_backend_rpg.model.UserSavedModule;

@Service
public class UserSavedModuleMapper {

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
        UserSavedModule entity = new UserSavedModule(
                dto.userId(),
                dto.moduleId(),
                dto.moduleVersionId(),
                dto.folderId(),
                dto.alias());
        return entity;
    }
}