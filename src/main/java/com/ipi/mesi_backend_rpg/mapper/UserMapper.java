package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.UserDTO;
import com.ipi.mesi_backend_rpg.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {

    public UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public User toEntity(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.username());
        user.setEmail(userDTO.email());
        user.setCreatedAt(userDTO.createdAt());
        user.setUpdatedAt(userDTO.updatedAt());
        return user;
    }

}
