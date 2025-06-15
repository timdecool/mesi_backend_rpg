package com.ipi.mesi_backend_rpg.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_subscription", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"subscriber_id", "subscribed_to_id"})
})
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private User subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscribed_to_id", nullable = false)
    private User subscribedTo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;

    public UserSubscription(User subscriber, User subscribedTo) {
        this.subscriber = subscriber;
        this.subscribedTo = subscribedTo;
        this.subscribedAt = LocalDateTime.now();
    }
}