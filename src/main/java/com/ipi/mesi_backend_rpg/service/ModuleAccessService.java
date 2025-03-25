package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.ModuleAccessDTO;
import com.ipi.mesi_backend_rpg.mapper.ModuleAccessMapper;
import com.ipi.mesi_backend_rpg.model.AccessRight;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleAccess;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.ModuleAccessRepository;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ModuleAccessService {


    ModuleAccessRepository moduleAccessRepository;
    ModuleAccessMapper moduleAccessMapper;
    ModuleRepository moduleRepository;

    public ModuleAccessService(ModuleAccessRepository moduleAccessRepository, ModuleAccessMapper moduleAccessMapper, ModuleRepository moduleRepository) {
        this.moduleAccessRepository = moduleAccessRepository;
        this.moduleAccessMapper = moduleAccessMapper;
        this.moduleRepository = moduleRepository;
    }
    
    public ModuleAccessDTO getModuleAccessById(Integer id) {
        ModuleAccess moduleAccess = moduleAccessRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return moduleAccessMapper.toDTO(moduleAccess);
    }

    public List<ModuleAccessDTO> getModuleAccessByModule(Module module) {
        List<ModuleAccess> moduleAccesses = moduleAccessRepository.findAllByModule(module);

        if (moduleAccesses == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return moduleAccesses.stream().map(moduleAccessMapper::toDTO).toList();
    }

    public ModuleAccessDTO getModuleAccessByUser(Module module, User user) {
        ModuleAccess moduleAccess = moduleAccessRepository.findModuleAccessBymoduleAndUser(module, user);

        return moduleAccessMapper.toDTO(moduleAccess);
    }

    public ModuleAccessDTO createModuleAccess(ModuleAccessDTO moduleAccessDTO, Long moduleId) {
        ModuleAccess moduleAccess = moduleAccessMapper.toEntity(moduleAccessDTO);
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        moduleAccess.setModule(module);
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

    public ModuleAccessDTO toggleAccessRight(Integer id, AccessRight accessRight) {
        ModuleAccess moduleAccess = moduleAccessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ModuleAccess not found with id: "));

        switch (accessRight) {
            case VIEW:
                moduleAccess.setCanView(!moduleAccess.isCanView());
                break;
            case EDIT:
                moduleAccess.setCanEdit(!moduleAccess.isCanEdit());
                break;
            case PUBLISH:
                moduleAccess.setCanPublish(!moduleAccess.isCanPublish());
                break;
            case INVITE:
                moduleAccess.setCanInvite(!moduleAccess.isCanInvite());
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid right type: " + accessRight + "only accept VIEW, EDIT, PUBLISH, INVITE");
        }

        ModuleAccess savedAccess = moduleAccessRepository.save(moduleAccess);
        return moduleAccessMapper.toDTO(savedAccess);
    }


}
