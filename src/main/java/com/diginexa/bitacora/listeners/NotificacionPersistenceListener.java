package com.diginexa.bitacora.listeners;

import com.diginexa.bitacora.entities.Notificacion;
import com.diginexa.bitacora.events.NotificacionEvent;
import com.diginexa.bitacora.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listener que persiste las notificaciones en la base de datos.
 * Se ejecuta primero (Order 1) para garantizar que la notificación
 * esté guardada antes de intentar entregarla por otros canales.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionPersistenceListener {

    private final NotificacionService notificacionService;

    @EventListener
    @Order(1)
    public Notificacion handleNotificacionEvent(NotificacionEvent event) {
        log.info("Persistiendo notificación tipo {} para usuario {}",
                event.getTipo(), event.getUsuarioDestinoId());

        Notificacion notificacion = notificacionService.crearNotificacion(
                event.getUsuarioDestinoId(),
                event.getTipo(),
                event.getPrioridad(),
                event.getTitulo(),
                event.getMensaje(),
                event.getDatosAdicionales()
        );

        log.debug("Notificación persistida con ID {}", notificacion.getId());
        return notificacion;
    }
}
