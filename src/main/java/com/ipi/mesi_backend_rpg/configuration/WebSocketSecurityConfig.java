package com.ipi.mesi_backend_rpg.configuration;

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
                }
                return message;
            }
        });
    }
}