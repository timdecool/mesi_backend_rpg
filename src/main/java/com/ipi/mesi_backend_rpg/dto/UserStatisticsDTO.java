package com.ipi.mesi_backend_rpg.dto;

public record UserStatisticsDTO(
        Long userId,
        Long modulesCreated,
        Long subscribersCount
) {
}