package com.diginexa.bitacora.listeners;

import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.events.NotificacionEvent;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import com.diginexa.bitacora.services.ConfiguracionNotificacionService;
import com.diginexa.bitacora.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener que envía notificaciones por correo electrónico.
 * Se ejecuta de forma asíncrona para no bloquear el flujo principal.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationListener {

    private final EmailService emailService;
    private final ConfiguracionNotificacionService configService;
    private final UsuarioRepository usuarioRepository;

    @EventListener
    @Order(3)
    @Async
    public void handleNotificacionEvent(NotificacionEvent event) {
        if (!configService.isEmailHabilitado()) {
            log.debug("Email deshabilitado, omitiendo envío para usuario {}",
                    event.getUsuarioDestinoId());
            return;
        }

        try {
            Usuario usuario = usuarioRepository.findById(event.getUsuarioDestinoId())
                    .orElse(null);

            if (usuario == null || usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
                log.warn("No se puede enviar email: usuario {} no encontrado o sin correo",
                        event.getUsuarioDestinoId());
                return;
            }

            log.info("Enviando email de notificación a {}", usuario.getCorreo());

            emailService.enviarNotificacion(
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    event.getTitulo(),
                    event.getMensaje(),
                    event.getPrioridad()
            );

            log.debug("Email enviado exitosamente a {}", usuario.getCorreo());

        } catch (Exception e) {
            log.error("Error enviando email de notificación a usuario {}: {}",
                    event.getUsuarioDestinoId(), e.getMessage());
        }
    }
}
