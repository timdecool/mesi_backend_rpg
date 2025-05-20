package com.ipi.mesi_backend_rpg.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.ipi.mesi_backend_rpg.dto.CursorPositionDTO;
import com.ipi.mesi_backend_rpg.dto.ModuleUpdateDTO;
import com.ipi.mesi_backend_rpg.service.CollaborativeEditingService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CollaborativeEditingController {

    private final CollaborativeEditingService collaborativeEditingService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/module/{moduleId}/update")
    public void handleModuleUpdate(@DestinationVariable Long moduleId, @Payload ModuleUpdateDTO update) {
        // Vérifier les droits d'accès et traiter la mise à jour
        collaborativeEditingService.processUpdate(moduleId, update);

        // Diffuser la mise à jour à tous les clients connectés à ce module
        messagingTemplate.convertAndSend("/module/" + moduleId + "/updates", update);
    }

    @MessageMapping("/module/{moduleId}/cursor")
    public void handleCursorPosition(@DestinationVariable Long moduleId, @Payload CursorPositionDTO cursorPosition) {
        // Vérifier les droits d'accès
        if (collaborativeEditingService.canAccessModule(moduleId, cursorPosition.getUserId())) {
            // Diffuser la position à tous les autres clients connectés
            messagingTemplate.convertAndSend("/module/" + moduleId + "/cursors", cursorPosition);
        }
    }
}