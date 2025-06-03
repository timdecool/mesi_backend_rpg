package com.ipi.mesi_backend_rpg.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Préfixe pour les topics auxquels les clients peuvent s'abonner
        config.enableSimpleBroker("/topic", "/queue", "/user", "/module");

        // Préfixe pour les endpoints que les clients peuvent appeler
        config.setApplicationDestinationPrefixes("/app");

        // Préfixe pour les messages destinés à un utilisateur spécifique
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point d'entrée WebSocket avec fallback SockJS
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200", "https://jdr-cli.vercel.app")
                .withSockJS();
    }
}