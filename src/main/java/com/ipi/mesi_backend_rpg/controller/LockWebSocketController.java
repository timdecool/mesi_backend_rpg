package com.ipi.mesi_backend_rpg.controller;

import com.ipi.mesi_backend_rpg.dto.lock.LockRequest;
import com.ipi.mesi_backend_rpg.dto.lock.ResourceLockCheckRequest;
import com.ipi.mesi_backend_rpg.service.EditLockService;
import com.ipi.mesi_backend_rpg.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LockWebSocketController {

    private final EditLockService lockService;
    private final SecurityUtils securityUtils;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/lock/acquire")
    public void acquireLock(@Payload LockRequest request,   
            StompHeaderAccessor headerAccessor) {

        try {
            Long userId = securityUtils.getCurrentUserIdFromWebSocket(headerAccessor);

            log.debug("Demande d'acquisition de lock par utilisateur {}: {}/{}",
                    userId, request.getResourceType(), request.getResourceId());

            EditLockService.LockResult result = lockService.acquireLock(
                    request.getResourceType(),
                    request.getResourceId(),
                    userId,
                    request.getLockScope() != null ? request.getLockScope() : "FULL",
                    request.getDurationMinutes() != null ? request.getDurationMinutes() : 10);

            // Répondre directement à l'utilisateur
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/lock-response",
                    Map.of(
                            "type", "LOCK_ACQUISITION_RESULT",
                            "success", result.isAcquired(),
                            "lockToken", result.getLockToken(),
                            "message", result.getMessage(),
                            "expiresAt", result.getExpiresAt(),
                            "conflictingLock", result.getConflictingLock(),
                            "resourceType", request.getResourceType(),
                            "resourceId", request.getResourceId()));

            log.info("Lock acquisition result for user {}: {}", userId, result.isAcquired());

        } catch (Exception e) {
            log.error("Erreur lors de l'acquisition du lock", e);

            // Envoyer une réponse d'erreur
            try {
                Long userId = securityUtils.getCurrentUserIdFromWebSocket(headerAccessor);
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/lock-response",
                        Map.of(
                                "type", "LOCK_ACQUISITION_ERROR",
                                "success", false,
                                "message", "Erreur lors de l'acquisition du lock: " + e.getMessage()));
            } catch (Exception authError) {
                log.error("Erreur d'authentification lors de l'acquisition du lock", authError);
            }
        }
    }

    @MessageMapping("/lock/release")
    public void releaseLock(@Payload Map<String, String> request,
            StompHeaderAccessor headerAccessor) {

        try {
            Long userId = securityUtils.getCurrentUserIdFromWebSocket(headerAccessor);
            String lockToken = request.get("lockToken");

            log.debug("Demande de libération de lock par utilisateur {}: {}", userId, lockToken);

            boolean released = lockService.releaseLock(lockToken);

            Map<String, Object> response = Map.of(
                    "type", "LOCK_RELEASE_RESULT",
                    "success", released,
                    "lockToken", lockToken,
                    "message", released ? "Lock libéré avec succès" : "Échec de la libération du lock");

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/lock-response",
                    response);

            log.info("Lock release result for user {}: {}", userId, released);

        } catch (Exception e) {
            log.error("Erreur lors de la libération du lock", e);

            try {
                Long userId = securityUtils.getCurrentUserIdFromWebSocket(headerAccessor);
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/lock-response",
                        Map.of(
                                "type", "LOCK_RELEASE_ERROR",
                                "success", false,
                                "message", "Erreur lors de la libération: " + e.getMessage()));
            } catch (Exception authError) {
                log.error("Erreur d'authentification lors de la libération", authError);
            }
        }
    }

    @MessageMapping("/lock/heartbeat")
    public void heartbeat(@Payload Map<String, String> request,
            StompHeaderAccessor headerAccessor) {

        try {
            Long userId = securityUtils.getCurrentUserIdFromWebSocket(headerAccessor);
            String lockToken = request.get("lockToken");

            log.debug("Heartbeat reçu de l'utilisateur {} pour le lock {}", userId, lockToken);

            boolean renewed = lockService.renewLock(lockToken);

            if (!renewed) {
                // Lock expiré ou introuvable
                Map<String, Object> response = Map.of(
                        "type", "LOCK_EXPIRED",
                        "lockToken", lockToken,
                        "message", "Le lock a expiré ou n'existe plus");

                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/lock-updates",
                        response);

                log.warn("Lock expired or not found for user {}: {}", userId, lockToken);
            } else {
                log.debug("Lock renewed successfully for user {}: {}", userId, lockToken);
            }

        } catch (Exception e) {
            log.error("Erreur lors du heartbeat", e);
        }
    }

    @MessageMapping("/lock/list")
    public void listUserLocks(StompHeaderAccessor headerAccessor) {

        try {
            Long userId = securityUtils.getCurrentUserIdFromWebSocket(headerAccessor);

            log.debug("Demande de liste des locks pour l'utilisateur {}", userId);

            List<EditLockService.LockInfo> locks = lockService.getUserActiveLocks(userId);

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/lock-response",
                    Map.of(
                            "type", "USER_LOCKS_LIST",
                            "userLocks", locks,
                            "count", locks.size()));

            log.debug("Envoi de {} locks actifs pour l'utilisateur {}", locks.size(), userId);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des locks utilisateur", e);

            try {
                Long userId = securityUtils.getCurrentUserIdFromWebSocket(headerAccessor);
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/lock-response",
                        Map.of(
                                "type", "USER_LOCKS_ERROR",
                                "message", "Erreur lors de la récupération des locks"));
            } catch (Exception authError) {
                log.error("Erreur d'authentification lors de la liste des locks", authError);
            }
        }
    }

    @MessageMapping("/lock/check")
    public void checkResourceLocks(@Payload ResourceLockCheckRequest request,
            StompHeaderAccessor headerAccessor) {

        try {
            Long userId = securityUtils.getCurrentUserIdFromWebSocket(headerAccessor);

            log.debug("Vérification des locks pour la ressource {}/{} par l'utilisateur {}",
                    request.getResourceType(), request.getResourceId(), userId);

            List<EditLockService.LockInfo> locks = lockService.getResourceLocks(
                    request.getResourceType(),
                    request.getResourceId(),
                    userId);

            boolean canModify = lockService.canUserModifyResource(
                    request.getResourceType(),
                    request.getResourceId(),
                    userId,
                    request.getRequiredScope() != null ? request.getRequiredScope() : "FULL");

            Map<String, Object> response = Map.of(
                    "type", "RESOURCE_LOCKS_CHECK",
                    "resourceLocks", locks,
                    "canModify", canModify,
                    "resourceType", request.getResourceType(),
                    "resourceId", request.getResourceId(),
                    "userId", userId);

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/lock-response",
                    response);

            log.debug("Résultat de vérification pour l'utilisateur {}: canModify={}, locks={}",
                    userId, canModify, locks.size());

        } catch (Exception e) {
            log.error("Erreur lors de la vérification des locks de ressource", e);

            try {
                Long userId = securityUtils.getCurrentUserIdFromWebSocket(headerAccessor);
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/lock-response",
                        Map.of(
                                "type", "RESOURCE_LOCKS_ERROR",
                                "message", "Erreur lors de la vérification des locks"));
            } catch (Exception authError) {
                log.error("Erreur d'authentification lors de la vérification", authError);
            }
        }
    }

    @MessageMapping("/lock/force-release")
    public void forceReleaseLock(@Payload Map<String, String> request,
            StompHeaderAccessor headerAccessor) {

        try {
            Long userId = securityUtils.getCurrentUserIdFromWebSocket(headerAccessor);
            String lockToken = request.get("lockToken");

            // Vérifier les droits d'administration (à adapter selon votre système)
            if (!securityUtils.hasRole("ADMIN")) {
                messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/lock-response",
                        Map.of(
                                "type", "FORCE_RELEASE_ERROR",
                                "success", false,
                                "message", "Droits insuffisants pour forcer la libération"));
                return;
            }

            log.warn("Libération forcée du lock {} par l'admin {}", lockToken, userId);

            boolean released = lockService.forceReleaseLock(lockToken, userId);

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/lock-response",
                    Map.of(
                            "type", "FORCE_RELEASE_RESULT",
                            "success", released,
                            "lockToken", lockToken,
                            "message", released ? "Lock forcé libéré" : "Échec de la libération forcée"));

        } catch (Exception e) {
            log.error("Erreur lors de la libération forcée", e);
        }
    }
}