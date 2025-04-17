package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.UserProfileDTO;
import com.ipi.mesi_backend_rpg.mapper.UserProfileMapper;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.model.UserProfile;
import com.ipi.mesi_backend_rpg.repository.UserProfileRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final UserRepository userRepository;


    public UserProfileDTO create(UserProfileDTO userProfileDTO) {
        UserProfile userProfile = userProfileMapper.toEntity(userProfileDTO);
        userProfileRepository.save(userProfile);
        return userProfileMapper.toDTO(userProfile);
    }

    public UserProfileDTO update(Long id, UserProfileDTO userProfileDTO) {
        UserProfile userProfile = userProfileRepository.findById(userProfileDTO.id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userProfile not found"));
        if (!id.equals(userProfile.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and body do not match");
        }
        userProfileRepository.save(userProfile);
        return userProfileMapper.toDTO(userProfile);
    }

    public UserProfileDTO delete(Long id) {
        UserProfile userProfile = userProfileRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userProfile not found"));
        userProfileRepository.delete(userProfile);
        return userProfileMapper.toDTO(userProfile);
    }

    public List<UserProfileDTO> findAll() {
        List<UserProfile> userProfiles = userProfileRepository.findAll();
        return userProfiles.stream().map(userProfileMapper::toDTO).toList();
    }

    public UserProfileDTO findById(Long id) {
        UserProfile userProfile = userProfileRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "userProfile not found"));
        return userProfileMapper.toDTO(userProfile);
    }

    public UserProfileDTO findByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        UserProfile userProfile = userProfileRepository.findUserProfileByUser(user);
        return userProfileMapper.toDTO(userProfile);
    }


}
