package com.ipi.mesi_backend_rpg.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record UserSubscriptionDTO(
        Long id,
        UserDTO subscriber,
        UserDTO subscribedTo,
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime subscribedAt
) {
}