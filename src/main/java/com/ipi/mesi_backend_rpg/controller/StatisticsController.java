package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.PlatformStatisticsDTO;
import com.ipi.mesi_backend_rpg.dto.UserStatisticsDTO;
import com.ipi.mesi_backend_rpg.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @MessageMapping("/statistics/connect")
    @SendTo("/topic/statistics")
    public PlatformStatisticsDTO handleConnect() {
        return statisticsService.getPlatformStatistics();
    }

    @RestController
    @RequestMapping("/api/statistics")
    @RequiredArgsConstructor
    public static class StatisticsRestController {
        
        private final StatisticsService statisticsService;

        @GetMapping("/platform")
        public PlatformStatisticsDTO getPlatformStatistics() {
            return statisticsService.getPlatformStatistics();
        }

        @GetMapping("/user/{userId}")
        public UserStatisticsDTO getUserStatistics(@PathVariable Long userId) {
            long modulesCreated = statisticsService.getModulesCreatedByUser(userId);
            long subscribersCount = statisticsService.getSubscribersCountForUser(userId);
            return new UserStatisticsDTO(userId, modulesCreated, subscribersCount);
        }
    }
}