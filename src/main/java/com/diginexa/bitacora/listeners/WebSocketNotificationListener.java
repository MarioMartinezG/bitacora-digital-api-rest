package com.diginexa.bitacora.listeners;

import com.diginexa.bitacora.dtos.notificacion.NotificacionDTO;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.events.NotificacionEvent;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import com.diginexa.bitacora.services.ConfiguracionNotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Listener que envía notificaciones en tiempo real via WebSocket.
 * Se ejecuta de forma asíncrona para no bloquear el flujo principal.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConfiguracionNotificacionService configService;
    private final UsuarioRepository usuarioRepository;

    @EventListener
    @Order(2)
    @Async
    public void handleNotificacionEvent(NotificacionEvent event) {
        if (!configService.isWebSocketHabilitado()) {
            log.debug("WebSocket deshabilitado, omitiendo envío para usuario {}",
                    event.getUsuarioDestinoId());
            return;
        }

        try {
            // Buscar el correo del usuario destino (usado como username en WebSocket)
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(event.getUsuarioDestinoId());
            if (usuarioOpt.isEmpty()) {
                log.warn("Usuario {} no encontrado, no se puede enviar notificación WebSocket",
                        event.getUsuarioDestinoId());
                return;
            }

            // Usar getUsername() que devuelve la parte antes del @ (coincide con la sesión STOMP)
            String usernameDestino = usuarioOpt.get().getUsername();
            log.info("Enviando notificación WebSocket a usuario {} ({})",
                    event.getUsuarioDestinoId(), usernameDestino);

            NotificacionDTO dto = NotificacionDTO.builder()
                    .tipo(event.getTipo().name())
                    .prioridad(event.getPrioridad().name())
                    .severity(event.getPrioridad().getSeverity())
                    .titulo(event.getTitulo())
                    .mensaje(event.getMensaje())
                    .datosAdicionales(event.getDatosAdicionales())
                    .leida(false)
                    .fechaCreacion(LocalDateTime.now())
                    .build();

            // Enviar al canal específico del usuario usando su username (parte antes del @)
            String destination = "/queue/notificaciones";
            messagingTemplate.convertAndSendToUser(
                    usernameDestino,
                    destination,
                    dto
            );

            log.debug("Notificación WebSocket enviada exitosamente a usuario {} ({})",
                    event.getUsuarioDestinoId(), usernameDestino);

        } catch (Exception e) {
            log.error("Error enviando notificación WebSocket a usuario {}: {}",
                    event.getUsuarioDestinoId(), e.getMessage());
        }
    }
}
