package com.ipi.mesi_backend_rpg.mapper;

import org.springframework.stereotype.Service;

import com.ipi.mesi_backend_rpg.dto.NotificationDTO;
import com.ipi.mesi_backend_rpg.model.Notification;

@Service
public class NotificationMapper {

    public NotificationDTO toDTO(Notification notification) {
        Long senderId = null;
        String senderUsername = "Système";
        
        if (notification.getSender() != null) {
            senderId = notification.getSender().getId();
            senderUsername = notification.getSender().getUsername();
        }
        
        return new NotificationDTO(
            notification.getId(),
            notification.getType(),
            notification.getContent(),
            notification.isRead(),
            notification.getCreatedAt(),
            notification.getRecipient().getId(),
            senderId,
            senderUsername,
            notification.getModule().getId(),
            notification.getModule().getTitle()
        );
    }
}