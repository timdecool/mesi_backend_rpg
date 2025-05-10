package com.ipi.mesi_backend_rpg.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ipi.mesi_backend_rpg.dto.UserSavedModuleDTO;
import com.ipi.mesi_backend_rpg.mapper.UserSavedModuleMapper;
import com.ipi.mesi_backend_rpg.model.UserSavedModule;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.UserSavedModuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSavedModuleService {
    private final UserSavedModuleRepository userSavedModuleRepository;
    private final UserSavedModuleMapper userSavedModuleMapper;
    private final ModuleRepository moduleRepository;

    public ResponseEntity<List<UserSavedModuleDTO>> getAllModulesByUserId(Long userId) {
        List<UserSavedModuleDTO> savedModules = userSavedModuleRepository.findByUserId(userId)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(savedModules, HttpStatus.OK);
    }

    public ResponseEntity<UserSavedModuleDTO> getSavedModuleById(Long savedModuleId) {
        return userSavedModuleRepository.findById(savedModuleId)
                .map(userSavedModuleMapper::toDTO)
                .map(module -> new ResponseEntity<>(module, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved module not found"));
    }

    public ResponseEntity<List<UserSavedModuleDTO>> getModulesByFolderId(Long folderId) {
        List<UserSavedModuleDTO> savedModules = userSavedModuleRepository.findByFolderId(folderId)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(savedModules, HttpStatus.OK);
    }

    public ResponseEntity<List<UserSavedModuleDTO>> getModulesByUserIdAndFolderId(Long userId, Long folderId) {
        List<UserSavedModuleDTO> savedModules = userSavedModuleRepository.findByUserIdAndFolderId(userId, folderId)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(savedModules, HttpStatus.OK);
    }

    public List<UserSavedModuleDTO> getModulesByUserIdAndModuleId(Long userId, Long moduleId) {
        return userSavedModuleRepository.findByUserIdAndModule_Id(userId, moduleId)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ResponseEntity<List<UserSavedModuleDTO>> searchModulesByAlias(Long userId, String alias) {
        List<UserSavedModuleDTO> savedModules = userSavedModuleRepository.findByUserIdAndAliasContaining(userId, alias)
                .stream()
                .map(userSavedModuleMapper::toDTO)
                .collect(Collectors.toList());
        return new ResponseEntity<>(savedModules, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<UserSavedModuleDTO> createSavedModule(UserSavedModuleDTO userSavedModuleDTO) {
        UserSavedModule userSavedModule = userSavedModuleMapper.toEntity(userSavedModuleDTO);
        UserSavedModule savedModule = userSavedModuleRepository.save(userSavedModule);
        UserSavedModuleDTO createdModule = userSavedModuleMapper.toDTO(savedModule);
        return new ResponseEntity<>(createdModule, HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<UserSavedModuleDTO> updateSavedModule(Long savedModuleId,
            UserSavedModuleDTO userSavedModuleDTO) {
        return userSavedModuleRepository.findById(savedModuleId)
                .map(savedModule -> {
                    moduleRepository.findById(userSavedModuleDTO.moduleId())
                            .ifPresent(savedModule::setModule);
                    savedModule.setModuleVersionId(userSavedModuleDTO.moduleVersionId());
                    savedModule.setFolderId(userSavedModuleDTO.folderId());
                    savedModule.setAlias(userSavedModuleDTO.alias());

                    UserSavedModule updatedModule = userSavedModuleRepository.save(savedModule);
                    return new ResponseEntity<>(userSavedModuleMapper.toDTO(updatedModule), HttpStatus.OK);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved module not found"));
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> moveModulesToFolder(Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> savedModuleIds = (List<Long>) request.get("savedModuleIds");
        Long targetFolderId = Long.valueOf(request.get("targetFolderId").toString());

        List<UserSavedModule> modules = userSavedModuleRepository.findAllById(savedModuleIds);

        if (modules.size() != savedModuleIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more modules not found");
        }

        modules.forEach(module -> module.setFolderId(targetFolderId));
        userSavedModuleRepository.saveAll(modules);

        Map<String, Object> response = Map.of(
                "success", true,
                "message", savedModuleIds.size() + " modules moved to folder " + targetFolderId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Void> deleteSavedModule(Long savedModuleId) {
        if (!userSavedModuleRepository.existsById(savedModuleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved module not found");
        }

        userSavedModuleRepository.deleteById(savedModuleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> deleteModulesByFolderId(Long folderId) {
        List<UserSavedModule> modules = userSavedModuleRepository.findByFolderId(folderId);

        if (!modules.isEmpty()) {
            userSavedModuleRepository.deleteAll(modules);
        }

        Map<String, Object> response = Map.of(
                "deleted", modules.size(),
                "message", modules.size() + " saved modules deleted from folder " + folderId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}