package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.PictureDTO;
import com.ipi.mesi_backend_rpg.dto.UserProfileDTO;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.model.UserProfile;
import com.ipi.mesi_backend_rpg.repository.UserSubscriptionRepository;
import com.ipi.mesi_backend_rpg.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileMapper {

    private final PictureMapper pictureMapper;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserService userService;

    public UserProfileDTO toDTO(UserProfile userProfile) {

        PictureDTO pictureDTO = null;
        if (userProfile.getPicture() != null) {
            pictureDTO = pictureMapper.toDTO(userProfile.getPicture());
        }

        // Obtenir le nombre d'abonnés
        Long subscriberCount = userSubscriptionRepository.countSubscribersByUser(userProfile.getUser());
        
        // Vérifier si l'utilisateur actuel est abonné à ce profil
        User currentUser = userService.getAuthenticatedUserOrNull();
        Boolean isSubscribedByCurrentUser = false;
        if (currentUser != null) {
            isSubscribedByCurrentUser = userSubscriptionRepository.existsBySubscriberAndSubscribedTo(currentUser, userProfile.getUser());
        }

        return new UserProfileDTO(
                userProfile.getId(),
                userProfile.getDescription(),
                userProfile.getCreatedAt(),
                userProfile.getUpdatedAt(),
                userProfile.getIsPublic(),
                userProfile.getProfileViews(),
                subscriberCount,
                isSubscribedByCurrentUser,
                pictureDTO
        );
    }

    public UserProfile toEntity(UserProfileDTO userProfileDTO) {
        UserProfile userProfile = new UserProfile(
                userProfileDTO.description(),
                userProfileDTO.createdAt(),
                userProfileDTO.updatedAt()
        );

        if (userProfileDTO.isPublic() != null) {
            userProfile.setIsPublic(userProfileDTO.isPublic());
        }

        if(userProfileDTO.picture() != null) {
            userProfile.setPicture(pictureMapper.toEntity(userProfileDTO.picture()));
        }
        return userProfile;
    }
}
