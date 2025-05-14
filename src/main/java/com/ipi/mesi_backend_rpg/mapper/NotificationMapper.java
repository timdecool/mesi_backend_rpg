package com.ipi.mesi_backend_rpg.mapper;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.NotificationDTO;
import com.ipi.mesi_backend_rpg.model.Notification;

@Service
public class NotificationMapper {

    public NotificationDTO toDTO(Notification notification) {
        return new NotificationDTO(
            notification.getId(),
            notification.getType(),
            notification.getContent(),
            notification.isRead(),
            notification.getCreatedAt(),
            notification.getRecipient().getId(),
            notification.getSender().getId(),
            notification.getSender().getUsername(),
            notification.getModule().getId(),
            notification.getModule().getTitle()
        );
    }
}