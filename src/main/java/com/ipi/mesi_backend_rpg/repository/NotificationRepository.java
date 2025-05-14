package com.ipi.mesi_backend_rpg.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ipi.mesi_backend_rpg.model.Notification;
import com.ipi.mesi_backend_rpg.model.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    List<Notification> findByRecipientAndReadOrderByCreatedAtDesc(User recipient, boolean read);

    long countByRecipientAndRead(User recipient, boolean read);
}