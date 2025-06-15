package com.ipi.mesi_backend_rpg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record UserProfileDTO(
        Long id,
        String description,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate updatedAt,

        Boolean isPublic,
        Long profileViews,
        Long subscriberCount,
        Boolean isSubscribedByCurrentUser,

        PictureDTO picture
) {
}
