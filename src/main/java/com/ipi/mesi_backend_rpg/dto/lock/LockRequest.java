package com.ipi.mesi_backend_rpg.dto.lock;

import com.ipi.mesi_backend_rpg.model.ResourceType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LockRequest {
    private ResourceType resourceType;
    private Long resourceId;
    private String lockScope;
    private Integer durationMinutes;
}
