package com.ipi.mesi_backend_rpg.mapper;

import com.ipi.mesi_backend_rpg.dto.UserSubscriptionDTO;
import com.ipi.mesi_backend_rpg.model.UserSubscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSubscriptionMapper {

    private final UserMapper userMapper;

    public UserSubscriptionDTO toDTO(UserSubscription userSubscription) {
        return new UserSubscriptionDTO(
                userSubscription.getId(),
                userMapper.toDTO(userSubscription.getSubscriber()),
                userMapper.toDTO(userSubscription.getSubscribedTo()),
                userSubscription.getSubscribedAt()
        );
    }
}