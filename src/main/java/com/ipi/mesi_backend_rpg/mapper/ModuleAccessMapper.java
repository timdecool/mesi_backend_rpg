package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.ModuleAccessDTO;
import com.ipi.mesi_backend_rpg.model.ModuleAccess;
import org.springframework.stereotype.Service;

@Service
public class ModuleAccessMapper {

    public ModuleAccessDTO toDTO(ModuleAccess moduleAccess) {
        return new ModuleAccessDTO(
                moduleAccess.getId(),
                moduleAccess.getModule(),
                moduleAccess.getUser(),
                moduleAccess.isCanView(),
                moduleAccess.isCanEdit(),
                moduleAccess.isCanPublish(),
                moduleAccess.isCanInvite()
        );
    }

    public ModuleAccess toEntity(ModuleAccessDTO moduleAccessDTO) {
        ModuleAccess moduleAccess = new ModuleAccess();
        moduleAccess.setId(moduleAccessDTO.id());
        moduleAccess.setModule(moduleAccessDTO.module());
        moduleAccess.setUser(moduleAccessDTO.user());
        moduleAccess.setCanView(moduleAccessDTO.canView());
        moduleAccess.setCanEdit(moduleAccessDTO.canEdit());
        moduleAccess.setCanPublish(moduleAccessDTO.canPublish());
        moduleAccess.setCanInvite(moduleAccessDTO.canInvite());
        return moduleAccess;
    }
}
