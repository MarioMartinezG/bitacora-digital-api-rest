package com.diginexa.bitacora.events;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Evento emitido cuando un estudiante solicita una sesión con su tutor.
 */
@Getter
public class SolicitudSesionEvent extends NotificacionEvent {

    private final Long solicitudId;
    private final Integer estudianteId;
    private final String nombreEstudiante;
    private final String motivo;

    public SolicitudSesionEvent(
            Object source,
            Integer tutorId,
            Long solicitudId,
            Integer estudianteId,
            String nombreEstudiante,
            String motivo) {
        super(
            source,
            tutorId,
            TipoNotificacion.SOLICITUD_SESION,
            PrioridadNotificacion.INFO,
            "Nueva solicitud de sesión",
            "El estudiante " + nombreEstudiante + " ha solicitado una sesión contigo.",
            crearDatosAdicionales(solicitudId, estudianteId, nombreEstudiante, motivo)
        );
        this.solicitudId = solicitudId;
        this.estudianteId = estudianteId;
        this.nombreEstudiante = nombreEstudiante;
        this.motivo = motivo;
    }

    private static Map<String, Object> crearDatosAdicionales(
            Long solicitudId,
            Integer estudianteId,
            String nombreEstudiante,
            String motivo) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("solicitudId", solicitudId);
        datos.put("estudianteId", estudianteId);
        datos.put("nombreEstudiante", nombreEstudiante);
        datos.put("motivo", motivo);
        return datos;
    }
}
