package com.ipi.mesi_backend_rpg.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ipi.mesi_backend_rpg.dto.NotificationDTO;
import com.ipi.mesi_backend_rpg.mapper.NotificationMapper;
import com.ipi.mesi_backend_rpg.model.Module;
import com.ipi.mesi_backend_rpg.model.Notification;
import com.ipi.mesi_backend_rpg.model.NotificationType;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.NotificationRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Transactional
    public Notification createNotification(NotificationType type, String content,
            User recipient, User sender, Module module) {

        if (recipient.equals(sender)) {
            log.debug("Skipping self-notification for user: {}", recipient.getId());
            return null; // Éviter de notifier l'utilisateur de ses propres actions
        }

        Notification notification = new Notification(type, content, recipient, sender, module);
        notification = notificationRepository.save(notification);

        // Envoyer la notification en temps réel via WebSocket
        NotificationDTO notificationDTO = notificationMapper.toDTO(notification);

        // Le destination est formatée comme /user/{userId}/queue/notifications
        messagingTemplate.convertAndSendToUser(
                recipient.getId().toString(),
                "/queue/notifications",
                notificationDTO);

        log.debug("Notification sent to user {}: {}", recipient.getId(), content);

        return notification;
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return notificationRepository.findByRecipientAndReadOrderByCreatedAtDesc(user, false).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return notificationRepository.countByRecipientAndRead(user, false);
    }

    @Transactional
    public NotificationDTO markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        notification.setRead(true);
        notification = notificationRepository.save(notification);

        return notificationMapper.toDTO(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientAndReadOrderByCreatedAtDesc(user, false);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(unreadNotifications);
    }
}