package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.ModuleAccessDTO;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.ModuleAccess;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ModuleAccessMapper {

    public final ModuleRepository moduleRepository;
    public final UserRepository userRepository;

    public ModuleAccessMapper(ModuleRepository moduleRepository, UserRepository userRepository) {
        this.moduleRepository = moduleRepository;
        this.userRepository = userRepository;
    }

    public ModuleAccessDTO toDTO(ModuleAccess moduleAccess) {
        return new ModuleAccessDTO(
                moduleAccess.getId(),
                moduleAccess.getModule().getId(),
                moduleAccess.getUser().getId(),
                moduleAccess.isCanView(),
                moduleAccess.isCanEdit(),
                moduleAccess.isCanPublish(),
                moduleAccess.isCanInvite()
        );
    }

    public ModuleAccess toEntity(ModuleAccessDTO moduleAccessDTO) {

        Module module = moduleRepository.findById(moduleAccessDTO.moduleId())
                .orElseThrow(() -> new IllegalArgumentException("Module non trouvé"));

        User user = userRepository.findById(moduleAccessDTO.userId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        ModuleAccess moduleAccess = new ModuleAccess();
        moduleAccess.setId(moduleAccessDTO.id());
        moduleAccess.setModule(module);
        moduleAccess.setUser(user);
        moduleAccess.setCanView(moduleAccessDTO.canView());
        moduleAccess.setCanEdit(moduleAccessDTO.canEdit());
        moduleAccess.setCanPublish(moduleAccessDTO.canPublish());
        moduleAccess.setCanInvite(moduleAccessDTO.canInvite());

        return moduleAccess;
    }
}
