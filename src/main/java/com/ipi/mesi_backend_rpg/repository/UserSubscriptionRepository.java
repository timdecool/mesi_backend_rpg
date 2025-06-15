package com.ipi.mesi_backend_rpg.repository;

import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    Optional<UserSubscription> findBySubscriberAndSubscribedTo(User subscriber, User subscribedTo);
    
    boolean existsBySubscriberAndSubscribedTo(User subscriber, User subscribedTo);
    
    List<UserSubscription> findBySubscriber(User subscriber);
    
    List<UserSubscription> findBySubscribedTo(User subscribedTo);
    
    @Query("SELECT COUNT(us) FROM UserSubscription us WHERE us.subscribedTo = :user")
    Long countSubscribersByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(us) FROM UserSubscription us WHERE us.subscriber = :user")
    Long countSubscriptionsByUser(@Param("user") User user);
    
    void deleteBySubscriberAndSubscribedTo(User subscriber, User subscribedTo);
}