package com.diginexa.bitacora.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket usando STOMP sobre SockJS.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitar broker simple para destinos /queue y /topic
        config.enableSimpleBroker("/queue", "/topic");

        // Prefijo para mensajes enviados desde el cliente
        config.setApplicationDestinationPrefixes("/app");

        // Prefijo para destinos específicos de usuario
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint principal para conexiones WebSocket
        registry.addEndpoint("/ws/notificaciones")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Endpoint sin SockJS para clientes que soporten WebSocket nativo
        registry.addEndpoint("/ws/notificaciones")
                .setAllowedOriginPatterns("*");
    }
}
