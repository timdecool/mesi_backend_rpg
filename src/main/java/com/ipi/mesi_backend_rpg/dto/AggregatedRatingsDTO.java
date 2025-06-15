package com.ipi.mesi_backend_rpg.dto;

public record AggregatedRatingsDTO(
        int moduleNumberOfRatings,
        int versionNumberOfRatings,
        Float moduleAverageRating,
        Float versionAverageRating,
        ModuleRatingDTO userRating
) {
}
