package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.PictureDTO;
import com.ipi.mesi_backend_rpg.dto.UserProfileDTO;
import com.ipi.mesi_backend_rpg.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileMapper {

    private final PictureMapper pictureMapper;

    public UserProfileDTO toDTO(UserProfile userProfile) {

        PictureDTO pictureDTO = null;
        if (userProfile.getPicture() != null) {
            pictureDTO = pictureMapper.toDTO(userProfile.getPicture());
        }

        return new UserProfileDTO(
                userProfile.getId(),
                userProfile.getDescription(),
                userProfile.getCreatedAt(),
                userProfile.getUpdatedAt(),
                pictureDTO
        );
    }

    public UserProfile toEntity(UserProfileDTO userProfileDTO) {
        UserProfile userProfile = new UserProfile(
                userProfileDTO.description(),
                userProfileDTO.createdAt(),
                userProfileDTO.updatedAt()
        );

        if(userProfileDTO.picture() != null) {
            userProfile.setPicture(pictureMapper.toEntity(userProfileDTO.picture()));
        }
        return userProfile;
    }
}
