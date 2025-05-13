package com.ipi.mesi_backend_rpg.configuration;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.ipi.mesi_backend_rpg.model.ModuleAccess;
import com.ipi.mesi_backend_rpg.model.User;
import com.ipi.mesi_backend_rpg.repository.ModuleAccessRepository;
import com.ipi.mesi_backend_rpg.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;
    private final ModuleAccessRepository moduleAccessRepository;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authToken = accessor.getFirstNativeHeader("Authorization");
                    log.debug("WebSocket connection attempt with token: {}", authToken != null ? "Present" : "Absent");

                    if (authToken != null && authToken.startsWith("Bearer ")) {
                        String token = authToken.substring(7);
                        try {
                            // Vérifier le token Firebase
                            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
                            String uid = decodedToken.getUid();
                            String email = decodedToken.getEmail();

                            log.debug("Authenticated WebSocket connection for user UID: {}, email: {}", uid, email);

                            // Trouver l'utilisateur interne correspondant par email
                            userRepository.findByEmail(email).ifPresent(user -> {
                                // Stocker l'ID de l'utilisateur dans la session WebSocket
                                accessor.setUser(() -> user.getId().toString());
                            });

                        } catch (FirebaseAuthException e) {
                            log.error("Invalid Firebase token for WebSocket connection", e);
                            throw new IllegalArgumentException("Invalid authentication token");
                        }
                    } else {
                        log.warn("Missing authentication token for WebSocket connection");
                        throw new IllegalArgumentException("Missing authentication token");
                    }
                    if (accessor != null &&
                            (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) ||
                                    StompCommand.SEND.equals(accessor.getCommand()))) {

                        String destination = accessor.getDestination();
                        if (destination != null && destination.startsWith("/user/")) {
                            // S'assurer que l'utilisateur ne souscrit qu'à ses propres notifications
                            String userId = accessor.getUser().getName();
                            if (!destination.contains("/user/" + userId + "/")) {
                                log.warn("User {} attempting to subscribe to another user's notifications: {}",
                                        userId, destination);
                                throw new IllegalArgumentException("Unauthorized subscription");
                            }
                        }

                        // Si le message concerne un module spécifique, vérifier les permissions
                        if (destination != null && destination.contains("/module/")) {
                            Long moduleId = extractModuleId(destination);
                            String userId = accessor.getUser().getName();

                            if (moduleId != null && userId != null) {
                                Long userIdLong = Long.parseLong(userId);
                                User user = userRepository.findById(userIdLong).orElse(null);

                                if (user != null) {
                                    // Vérifier si l'utilisateur a accès au module
                                    boolean hasAccess = checkModuleAccess(user, moduleId);
                                    if (!hasAccess) {
                                        log.warn("User {} attempting to access module {} without permission",
                                                userId, moduleId);
                                        throw new IllegalArgumentException("Unauthorized module access");
                                    }
                                }
                            }
                        }
                    }
                }
                return message;
            }

            private Long extractModuleId(String destination) {
                // "/topic/module/123" -> 123
                try {
                    String[] parts = destination.split("/");
                    for (int i = 0; i < parts.length; i++) {
                        if ("module".equals(parts[i]) && i + 1 < parts.length) {
                            return Long.parseLong(parts[i + 1]);
                        }
                    }
                } catch (Exception e) {
                    log.debug("Failed to extract module ID from: {}", destination);
                }
                return null;
            }

            private boolean checkModuleAccess(User user, Long moduleId) {
                // Vérifier si l'utilisateur a un ModuleAccess pour ce module
                List<ModuleAccess> accesses = moduleAccessRepository.findAllByUser(user);
                return accesses.stream()
                        .anyMatch(access -> access.getModule().getId() == moduleId && access.isCanView());
            }
        });
    }
}