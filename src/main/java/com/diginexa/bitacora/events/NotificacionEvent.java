package com.diginexa.bitacora.events;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase base abstracta para eventos de notificación.
 * Todos los eventos de notificación deben extender esta clase.
 */
@Getter
public abstract class NotificacionEvent extends ApplicationEvent {

    private final Integer usuarioDestinoId;
    private final TipoNotificacion tipo;
    private final PrioridadNotificacion prioridad;
    private final String titulo;
    private final String mensaje;
    private final Map<String, Object> datosAdicionales;

    protected NotificacionEvent(
            Object source,
            Integer usuarioDestinoId,
            TipoNotificacion tipo,
            PrioridadNotificacion prioridad,
            String titulo,
            String mensaje,
            Map<String, Object> datosAdicionales) {
        super(source);
        this.usuarioDestinoId = usuarioDestinoId;
        this.tipo = tipo;
        this.prioridad = prioridad;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.datosAdicionales = datosAdicionales != null ? datosAdicionales : new HashMap<>();
    }
}
