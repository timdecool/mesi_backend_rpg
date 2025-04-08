package com.ipi.mesi_backend_rpg.dto;

import com.ipi.mesi_backend_rpg.model.PictureUsage;

import java.time.LocalDate;

public record PictureDTO(
        Long id,
        PictureUsage pictureUsage,
        Long pictureUsageId,
        String title,
        String src,
        LocalDate createdAt,
        LocalDate updateAt) {
}
