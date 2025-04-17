package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.UserDTO;
import com.ipi.mesi_backend_rpg.mapper.UserMapper;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        userRepository.save(user);
        return userMapper.toDTO(user);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no user found"));

        return userMapper.toDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no use found for email " + email));
    
        return userMapper.toDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(userMapper::toDTO).toList();
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no user found"));

        if (!existingUser.getId().equals(userDTO.id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and body do not match");
        }

        User updatedUser = userMapper.toEntity(userDTO);
        updatedUser.setId(id);
        userRepository.save(updatedUser);
        return userMapper.toDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no user found"));
        userRepository.delete(user);
    }


}
