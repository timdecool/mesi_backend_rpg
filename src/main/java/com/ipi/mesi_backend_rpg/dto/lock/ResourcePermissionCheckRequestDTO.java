package com.ipi.mesi_backend_rpg.dto.lock;

import com.ipi.mesi_backend_rpg.model.ResourceType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourcePermissionCheckRequestDTO {
    private ResourceType resourceType;
    private Long resourceId;
    private String requiredScope;
}
