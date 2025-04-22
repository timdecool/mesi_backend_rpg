package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.UserProfileDTO;
import com.ipi.mesi_backend_rpg.model.UserProfile;
import org.springframework.stereotype.Service;

@Service
public class UserProfileMapper {

    public UserProfileDTO toDTO(UserProfile userProfile) {
        return new UserProfileDTO(
                userProfile.getId(),
                userProfile.getDescription(),
                userProfile.getCreatedAt(),
                userProfile.getUpdatedAt()
        );
    }

    public UserProfile toEntity(UserProfileDTO userProfileDTO) {
        return new UserProfile(
                userProfileDTO.description(),
                userProfileDTO.createdAt(),
                userProfileDTO.updatedAt()
        );
    }
}
