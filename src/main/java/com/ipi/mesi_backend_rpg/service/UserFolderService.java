package com.ipi.mesi_backend_rpg.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ipi.mesi_backend_rpg.dto.UserFolderDTO;
import com.ipi.mesi_backend_rpg.mapper.UserFolderMapper;
import com.ipi.mesi_backend_rpg.model.UserFolder;
import com.ipi.mesi_backend_rpg.repository.UserFolderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFolderService {

    private final UserFolderRepository userFolderRepository;
    private final UserFolderMapper userFolderMapper;

    public List<UserFolderDTO> getAllFoldersByUserId(Long userId) {
        return userFolderRepository.findByUserId(userId)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserFolderDTO> getFolderById(Long folderId) {
        return userFolderRepository.findById(folderId)
                .map(userFolderMapper::toDTO);
    }

    public List<UserFolderDTO> getChildFolders(Long parentFolderId) {
        return userFolderRepository.findByParentFolder(parentFolderId)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserFolderDTO> getRootFolders(Long userId) {
        return userFolderRepository.findByUserIdAndParentFolderIsNull(userId)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserFolderDTO> searchFoldersByName(Long userId, String name) {
        return userFolderRepository.findByUserIdAndNameContaining(userId, name)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserFolderDTO createFolder(UserFolderDTO userFolderDTO) {
        UserFolder userFolder = new UserFolder(
                userFolderDTO.userId(),
                userFolderDTO.name(),
                userFolderDTO.parentFolder());

        UserFolder savedFolder = userFolderRepository.save(userFolder);
        return userFolderMapper.toDTO(savedFolder);
    }

    @Transactional
    public Optional<UserFolderDTO> updateFolder(Long folderId, UserFolderDTO userFolderDTO) {
        return userFolderRepository.findById(folderId)
                .map(folder -> {
                    // Utilise les setters camelCase
                    folder.setName(userFolderDTO.name());
                    folder.setParentFolder(userFolderDTO.parentFolder());
                    UserFolder updatedFolder = userFolderRepository.save(folder);
                    return userFolderMapper.toDTO(updatedFolder);
                });
    }

    @Transactional
    public boolean deleteFolder(Long folderId) {
        if (userFolderRepository.existsById(folderId)) {
            userFolderRepository.deleteById(folderId);
            return true;
        }
        return false;
    }
}