package com.ipi.mesi_backend_rpg.service;

import com.ipi.mesi_backend_rpg.dto.PlatformStatisticsDTO;
import com.ipi.mesi_backend_rpg.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ModuleRepository moduleRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    public PlatformStatisticsDTO getPlatformStatistics() {
        long totalModules = moduleRepository.count();
        long activeUsersCount = activeUsers.size();
        long sharedModules = moduleRepository.countSharedModules();
        
        return new PlatformStatisticsDTO(totalModules, activeUsersCount, sharedModules);
    }

    public void addActiveUser(String userId) {
        activeUsers.add(userId);
        broadcastStatistics();
    }

    public void removeActiveUser(String userId) {
        activeUsers.remove(userId);
        broadcastStatistics();
    }

    public void broadcastStatistics() {
        PlatformStatisticsDTO statistics = getPlatformStatistics();
        messagingTemplate.convertAndSend("/topic/statistics", statistics);
    }

    public long getModulesCreatedByUser(Long userId) {
        return moduleRepository.countByCreator_Id(userId);
    }

    public long getSubscribersCountForUser(Long userId) {
        return moduleRepository.countSubscribersByCreatorId(userId);
    }
}