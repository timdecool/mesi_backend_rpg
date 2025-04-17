package com.ipi.mesi_backend_rpg.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ipi.mesi_backend_rpg.dto.UserSavedModuleDTO;
import com.ipi.mesi_backend_rpg.mapper.UserSavedModuleMapper;
import com.ipi.mesi_backend_rpg.model.UserSavedModule;
import com.ipi.mesi_backend_rpg.repository.UserSavedModuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSavedModuleService {
    private final UserSavedModuleRepository userSavedModuleRepository;
    private final UserSavedModuleMapper userSavedModuleMapper;

    public List<UserSavedModuleDTO> getAllModulesByUserId(Long userId) {
        return userSavedModuleRepository.findByUserId(userId)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserSavedModuleDTO> getSavedModuleById(Long savedModuleId) {
        return userSavedModuleRepository.findById(savedModuleId)
                .map(userSavedModuleMapper::toDTO);
    }

    public List<UserSavedModuleDTO> getModulesByFolderId(Long folderId) {
        return userSavedModuleRepository.findByFolderId(folderId)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserSavedModuleDTO> getModulesByUserIdAndFolderId(Long userId, Long folderId) {
        return userSavedModuleRepository.findByUserIdAndFolderId(userId, folderId)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserSavedModuleDTO> getModulesByUserIdAndModuleId(Long userId, Long moduleId) {
        return userSavedModuleRepository.findByUserIdAndModuleId(userId, moduleId)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<UserSavedModuleDTO> searchModulesByAlias(Long userId, String alias) {
        return userSavedModuleRepository.findByUserIdAndAliasContaining(userId, alias)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserSavedModuleDTO createSavedModule(UserSavedModuleDTO userSavedModuleDTO) {
        UserSavedModule userSavedModule = userSavedModuleMapper.toEntity(userSavedModuleDTO);
        UserSavedModule savedModule = userSavedModuleRepository.save(userSavedModule);
        return userSavedModuleMapper.toDTO(savedModule);
    }

    @Transactional
    public Optional<UserSavedModuleDTO> updateSavedModule(Long savedModuleId, UserSavedModuleDTO userSavedModuleDTO) {
        return userSavedModuleRepository.findById(savedModuleId)
                .map(module -> {
                    // Utilise les setters camelCase
                    module.setModuleId(userSavedModuleDTO.moduleId());
                    module.setModuleVersionId(userSavedModuleDTO.moduleVersionId());
                    module.setFolderId(userSavedModuleDTO.folderId());
                    module.setAlias(userSavedModuleDTO.alias());
                    UserSavedModule updatedModule = userSavedModuleRepository.save(module);
                    return userSavedModuleMapper.toDTO(updatedModule);
                });
    }

    @Transactional
    public boolean moveModulesToFolder(List<Long> savedModuleIds, Long targetFolderId) {
        List<UserSavedModule> modules = userSavedModuleRepository.findAllById(savedModuleIds);

        if (modules.size() != savedModuleIds.size()) {
            return false;
        }
        // Utilise le setter camelCase
        modules.forEach(module -> module.setFolderId(targetFolderId));
        userSavedModuleRepository.saveAll(modules);

        return true;
    }

    @Transactional
    public boolean deleteSavedModule(Long savedModuleId) {
        if (userSavedModuleRepository.existsById(savedModuleId)) {
            userSavedModuleRepository.deleteById(savedModuleId);
            return true;
        }
        return false;
    }

    @Transactional
    public long deleteModulesByFolderId(Long folderId) {
        List<UserSavedModule> modules = userSavedModuleRepository.findByFolderId(folderId);
        if (!modules.isEmpty()) {
            userSavedModuleRepository.deleteAll(modules);
        }
        return modules.size();
    }
}