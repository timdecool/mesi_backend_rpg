package com.ipi.mesi_backend_rpg.mapper;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.UserFolderDTO;
import com.ipi.mesi_backend_rpg.model.UserFolder;

@Service
public class UserFolderMapper {

    public UserFolderDTO toDTO(UserFolder userFolder) {
        return new UserFolderDTO(
                userFolder.getFolder_id(),
                userFolder.getUser_id(),
                userFolder.getName(),
                userFolder.getParent_folder());
    }

}
