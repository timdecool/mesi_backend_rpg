package com.ipi.mesi_backend_rpg.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ipi.mesi_backend_rpg.model.NotificationType;

public record NotificationDTO(
    Long id,
    NotificationType type,
    String content,
    boolean read,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,
    Long recipientId,
    Long senderId,
    String senderUsername,
    Long moduleId,
    String moduleTitle
) {}