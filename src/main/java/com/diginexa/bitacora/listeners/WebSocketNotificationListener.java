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
            // Obtener el username del usuario destino para que coincida con la autenticación WebSocket
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(event.getUsuarioDestinoId());
            if (usuarioOpt.isEmpty()) {
                log.warn("Usuario destino {} no encontrado, omitiendo envío WebSocket",
                        event.getUsuarioDestinoId());
                return;
            }

            String username = usuarioOpt.get().getUsername();
            log.info("Enviando notificación WebSocket a usuario {} (username: {})",
                    event.getUsuarioDestinoId(), username);

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

            // Enviar al canal específico del usuario usando el username
            String destination = "/queue/notificaciones";
            messagingTemplate.convertAndSendToUser(
                    username,
                    destination,
                    dto
            );

            log.debug("Notificación WebSocket enviada exitosamente a usuario {} (username: {})",
                    event.getUsuarioDestinoId(), username);

        } catch (Exception e) {
            log.error("Error enviando notificación WebSocket a usuario {}: {}",
                    event.getUsuarioDestinoId(), e.getMessage());
        }
    }
}
