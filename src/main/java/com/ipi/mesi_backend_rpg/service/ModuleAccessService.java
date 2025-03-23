package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.ModuleAccessDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleAccessMapper;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleAccess;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.ModuleAccessRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ModuleAccessService {

    ModuleAccessRepository moduleAccessRepository;
    ModuleAccessMapper moduleAccessMapper;

    public ModuleAccessService(ModuleAccessRepository moduleAccessRepository, ModuleAccessMapper moduleAccessMapper) {
        this.moduleAccessRepository = moduleAccessRepository;
        this.moduleAccessMapper = moduleAccessMapper;
    }

    public List<ModuleAccessDTO> getAllModuleAccesses() {
        return moduleAccessRepository.findAll()
                .stream()
                .map(moduleAccessMapper::toDTO).toList();
    }

    public ModuleAccessDTO getModuleAccessById(Integer id) {
        ModuleAccess moduleAccess = moduleAccessRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return moduleAccessMapper.toDTO(moduleAccess);
    }

    public ModuleAccessDTO getModuleAccessByModule(Module module) {
        ModuleAccess moduleAccess = moduleAccessRepository.findByModule(module);

        if (moduleAccess == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return moduleAccessMapper.toDTO(moduleAccess);
    }

    public List<ModuleAccessDTO> getAllModuleAccessByUser(User user) {
        return moduleAccessRepository.findAllByUser(user)
                .stream()
                .map(moduleAccessMapper::toDTO).toList();
    }

    public ModuleAccessDTO createModuleAccess(ModuleAccessDTO moduleAccessDTO) {
        ModuleAccess moduleAccess = moduleAccessMapper.toEntity(moduleAccessDTO);
        moduleAccess = moduleAccessRepository.save(moduleAccess);
        return moduleAccessMapper.toDTO(moduleAccess);
    }

    public ModuleAccessDTO updateModuleAccess(Integer id, ModuleAccessDTO moduleAccessDTO) {

        ModuleAccess existingModuleAccess = moduleAccessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ModuleAccess not found"));

        if (!existingModuleAccess.getId().equals(moduleAccessDTO.id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID in path and body do not match");
        }

        ModuleAccess updatedModuleAccess = moduleAccessMapper.toEntity(moduleAccessDTO);
        updatedModuleAccess.setId(existingModuleAccess.getId());
        moduleAccessRepository.save(updatedModuleAccess);
        return moduleAccessMapper.toDTO(updatedModuleAccess);
    }

    public ModuleAccessDTO deleteModuleAccess(Integer id) {
        ModuleAccess moduleAccess = moduleAccessRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        moduleAccessRepository.delete(moduleAccess);
        return moduleAccessMapper.toDTO(moduleAccess);
    }


}
