package com.ipi.mesi_backend_rpg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AggregatedRatingsDTO(
        int moduleNumberOfRatings,
        int versionNumberOfRatings,
        Float moduleAverageRating,
        Float versionAverageRating,
        ModuleRatingDTO userRating
) {
}
