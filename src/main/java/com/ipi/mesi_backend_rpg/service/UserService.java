package com.ipi.mesi_backend_rpg.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.ipi.mesi_backend_rpg.configuration.FirebaseAuthenticationFilter;
import com.ipi.mesi_backend_rpg.dto.UserDTO;
import com.ipi.mesi_backend_rpg.mapper.UserMapper;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FirebaseAuthenticationFilter firebaseAuthenticationFilter;

    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        userRepository.save(user);
        return userMapper.toDTO(user);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no user found"));

        return userMapper.toDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no use found for email " + email));

        return userMapper.toDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(userMapper::toDTO).toList();
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no user found"));

        if (userDTO.id() != null && !userDTO.id().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and body do not match");
        }

        existingUser.setUsername(userDTO.username());
        existingUser.setEmail(userDTO.email());

        existingUser.setUpdatedAt(java.time.LocalDateTime.now());
        User savedUser = userRepository.save(existingUser);

        return userMapper.toDTO(savedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no user found"));
        userRepository.delete(user);
    }

    public List<UserDTO> searchUsersByUsername(String query) {
        List<User> users = userRepository.findFirst20ByUsernameContainingIgnoreCase(query);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No users found with username containing: " + query);
        }
        return users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> searchUsersByEmail(String query) {
        List<User> users = userRepository.findFirst20ByEmailContainingIgnoreCase(query);
        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found with email containing: " + query);
        }
        return users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public User getAuthenticatedUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes()).getRequest();
            FirebaseToken token = firebaseAuthenticationFilter.getDecodedToken(request);
            return userRepository.findByEmail(token.getEmail()).orElseThrow();
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Invalid Firebase token", e);
        }
    }
}
