package com.ipi.mesi_backend_rpg.dto;

public record PlatformStatisticsDTO(
        long totalModulesCreated,
        long activeUsers,
        long sharedModules
) {
}