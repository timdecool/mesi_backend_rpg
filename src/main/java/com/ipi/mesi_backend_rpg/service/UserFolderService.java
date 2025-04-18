package com.ipi.mesi_backend_rpg.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    public ResponseEntity<List<UserFolderDTO>> getAllFoldersByUserId(Long userId) {
        List<UserFolderDTO> folders = userFolderRepository.findByUserId(userId)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(folders, HttpStatus.OK);
    }

    public ResponseEntity<UserFolderDTO> getFolderById(Long folderId) {
        return userFolderRepository.findById(folderId)
                .map(userFolderMapper::toDTO)
                .map(folder -> new ResponseEntity<>(folder, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
    }

    public ResponseEntity<List<UserFolderDTO>> getChildFolders(Long parentFolderId) {
        List<UserFolderDTO> childFolders = userFolderRepository.findByParentFolder(parentFolderId)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(childFolders, HttpStatus.OK);
    }

    public ResponseEntity<List<UserFolderDTO>> getRootFolders(Long userId) {
        List<UserFolderDTO> rootFolders = userFolderRepository.findByUserIdAndParentFolderIsNull(userId)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(rootFolders, HttpStatus.OK);
    }

    public ResponseEntity<List<UserFolderDTO>> searchFoldersByName(Long userId, String name) {
        List<UserFolderDTO> folders = userFolderRepository.findByUserIdAndNameContaining(userId, name)
                .stream()
                .map(userFolderMapper::toDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(folders, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<UserFolderDTO> createFolder(UserFolderDTO userFolderDTO) {
        UserFolder userFolder = new UserFolder(
                userFolderDTO.userId(),
                userFolderDTO.name(),
                userFolderDTO.parentFolder());

        UserFolder savedFolder = userFolderRepository.save(userFolder);
        UserFolderDTO createdFolder = userFolderMapper.toDTO(savedFolder);
        return new ResponseEntity<>(createdFolder, HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<UserFolderDTO> updateFolder(Long folderId, UserFolderDTO userFolderDTO) {
        return userFolderRepository.findById(folderId)
                .map(folder -> {
                    folder.setName(userFolderDTO.name());
                    folder.setParentFolder(userFolderDTO.parentFolder());
                    UserFolder updatedFolder = userFolderRepository.save(folder);
                    return new ResponseEntity<>(userFolderMapper.toDTO(updatedFolder), HttpStatus.OK);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
    }

    @Transactional
    public ResponseEntity<Void> deleteFolder(Long folderId) {
        if (!userFolderRepository.existsById(folderId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found");
        }

        userFolderRepository.deleteById(folderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}