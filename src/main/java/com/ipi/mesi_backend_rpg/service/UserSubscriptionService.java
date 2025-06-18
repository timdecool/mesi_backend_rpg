package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.UserSubscriptionDTO;
import com.ipi.mesi_backend_rpg.mapper.UserSubscriptionMapper;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.model.UserSubscription;
import com.ipi.mesi_backend_rpg.repository.UserRepository;
import com.ipi.mesi_backend_rpg.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionMapper userSubscriptionMapper;
    private final UserService userService;
    private final NotificationService notificationService; // Inject NotificationService

    @Transactional
    public UserSubscriptionDTO subscribe(Long subscribedToUserId) {
        User subscriber = userService.getAuthenticatedUser();
        User subscribedTo = userRepository.findById(subscribedToUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (subscriber.getId().equals(subscribedTo.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot subscribe to yourself");
        }

        if (userSubscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already subscribed to this user");
        }

        UserSubscription subscription = new UserSubscription(subscriber, subscribedTo);
        UserSubscription saved = userSubscriptionRepository.save(subscription);

        // Notify the subscribedTo user
        String content = subscriber.getUsername() + " s'est abonné à votre profil.";
        notificationService.createNotification(
                com.ipi.mesi_backend_rpg.model.NotificationType.MODULE_LIKE, // Using MODULE_LIKE as a generic "interaction" for now, consider adding a new type
                content,
                subscribedTo, // Recipient: the user being subscribed to
                subscriber,   // Sender: the user who subscribed
                null          // No specific module associated with a profile subscription
        );

        return userSubscriptionMapper.toDTO(saved);
    }

    @Transactional
    public void unsubscribe(Long subscribedToUserId) {
        User subscriber = userService.getAuthenticatedUser();
        User subscribedTo = userRepository.findById(subscribedToUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserSubscription subscription = userSubscriptionRepository.findBySubscriberAndSubscribedTo(subscriber, subscribedTo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));

        userSubscriptionRepository.delete(subscription);
    }

    public List<UserSubscriptionDTO> getSubscriptions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserSubscription> subscriptions = userSubscriptionRepository.findBySubscriber(user);
        return subscriptions.stream()
                .map(userSubscriptionMapper::toDTO)
                .toList();
    }

    public List<UserSubscriptionDTO> getSubscribers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserSubscription> subscribers = userSubscriptionRepository.findBySubscribedTo(user);
        return subscribers.stream()
                .map(userSubscriptionMapper::toDTO)
                .toList();
    }

    public Long getSubscriberCount(User user) {
        return userSubscriptionRepository.countSubscribersByUser(user);
    }

    public boolean isSubscribed(User subscriber, User subscribedTo) {
        if (subscriber == null || subscribedTo == null) {
            return false;
        }
        return userSubscriptionRepository.existsBySubscriberAndSubscribedTo(subscriber, subscribedTo);
    }
}