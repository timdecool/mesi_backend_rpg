package com.ipi.mesi_backend_rpg.controller;


import com.ipi.mesi_backend_rpg.dto.UserProfileDTO;
import com.ipi.mesi_backend_rpg.dto.UserSubscriptionDTO;
import com.ipi.mesi_backend_rpg.service.UserProfileService;
import com.ipi.mesi_backend_rpg.service.UserSubscriptionService;
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
    private final UserSubscriptionService userSubscriptionService;
    
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

    @PostMapping("/user/{userId}/toggle-privacy")
    public ResponseEntity<UserProfileDTO> toggleProfilePrivacy(@PathVariable Long userId) {
        UserProfileDTO userProfileDTO = userProfileService.togglePrivacy(userId);
        return new ResponseEntity<>(userProfileDTO, HttpStatus.OK);
    }

    @PostMapping("/user/{userId}/subscribe")
    public ResponseEntity<UserSubscriptionDTO> subscribeToUser(@PathVariable Long userId) {
        UserSubscriptionDTO subscription = userSubscriptionService.subscribe(userId);
        return new ResponseEntity<>(subscription, HttpStatus.CREATED);
    }

    @DeleteMapping("/user/{userId}/unsubscribe")
    public ResponseEntity<Void> unsubscribeFromUser(@PathVariable Long userId) {
        userSubscriptionService.unsubscribe(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/user/{userId}/subscriptions")
    public ResponseEntity<List<UserSubscriptionDTO>> getUserSubscriptions(@PathVariable Long userId) {
        List<UserSubscriptionDTO> subscriptions = userSubscriptionService.getSubscriptions(userId);
        return new ResponseEntity<>(subscriptions, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/subscribers")
    public ResponseEntity<List<UserSubscriptionDTO>> getUserSubscribers(@PathVariable Long userId) {
        List<UserSubscriptionDTO> subscribers = userSubscriptionService.getSubscribers(userId);
        return new ResponseEntity<>(subscribers, HttpStatus.OK);
    }

    @PostMapping("/user/{userId}/views/increment")
    public ResponseEntity<UserProfileDTO> incrementProfileViews(@PathVariable Long userId) {
        UserProfileDTO userProfileDTO = userProfileService.incrementViews(userId);
        return new ResponseEntity<>(userProfileDTO, HttpStatus.OK);
    }

    @PostMapping("/user/{userId}/views/decrement")
    public ResponseEntity<UserProfileDTO> decrementProfileViews(@PathVariable Long userId) {
        UserProfileDTO userProfileDTO = userProfileService.decrementViews(userId);
        return new ResponseEntity<>(userProfileDTO, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/views")
    public ResponseEntity<Long> getProfileViews(@PathVariable Long userId) {
        Long views = userProfileService.getProfileViews(userId);
        return new ResponseEntity<>(views, HttpStatus.OK);
    }

}
