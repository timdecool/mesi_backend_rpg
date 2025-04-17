package com.ipi.mesi_backend_rpg.mapper;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.UserSavedModuleDTO;
import com.ipi.mesi_backend_rpg.model.UserSavedModule;

@Service
public class UserSavedModuleMapper {
    public UserSavedModuleDTO toDTO(UserSavedModule userSavedModule) {
        return new UserSavedModuleDTO(
                userSavedModule.getSaved_module_id(),
                userSavedModule.getUser_id(),
                userSavedModule.getModule_id(),
                userSavedModule.getModule_version_id(),
                userSavedModule.getFolder_id(),
                userSavedModule.getAlias());
    }

    public UserSavedModule toEntity(UserSavedModuleDTO dto) {
        return new UserSavedModule(
                dto.saved_module_id(),
                dto.user_id(),
                dto.module_id(),
                dto.module_version_id(),
                dto.folder_id(),
                dto.alias());
    }
}
