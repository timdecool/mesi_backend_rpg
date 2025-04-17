package com.ipi.mesi_backend_rpg.controller;


import com.ipi.mesi_backend_rpg.dto.UserProfileDTO;
import com.ipi.mesi_backend_rpg.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-profile")
public class UserProfileController {

    private final UserProfileService userProfileService;
    
    @PostMapping
    public ResponseEntity<UserProfileDTO> createUserProfile(@Valid @RequestBody UserProfileDTO userProfileDTO) {
        userProfileService.create(userProfileDTO);
        return new ResponseEntity<>(userProfileDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserProfileDTO>> getAllUserProfiles() {
        List<UserProfileDTO> userProfiles = userProfileService.findAll();
        return new ResponseEntity<>(userProfiles, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getUserProfileById(@PathVariable Long id) {
        UserProfileDTO userProfileDTO = userProfileService.findById(id);
        return new ResponseEntity<>(userProfileDTO, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfileDTO> getUserProfileByUserId(@PathVariable Long userId) {
        UserProfileDTO userProfileDTO = userProfileService.findByUserId(userId);
        return new ResponseEntity<>(userProfileDTO, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfileDTO> updateUserProfile(@PathVariable Long id, @Valid @RequestBody UserProfileDTO userProfileDTO) {
        UserProfileDTO userProfileDTOUpdated = userProfileService.update(id, userProfileDTO);
        return new ResponseEntity<>(userProfileDTOUpdated, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<UserProfileDTO> deleteUserProfile(@PathVariable Long id) {
        UserProfileDTO userProfileDTO = userProfileService.findById(id);
        userProfileService.delete(id);
        return new ResponseEntity<>(userProfileDTO, HttpStatus.NO_CONTENT);
    }

}
