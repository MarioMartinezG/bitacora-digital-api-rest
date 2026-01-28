package com.diginexa.bitacora.events;

import com.diginexa.bitacora.constants.EstadoSolicitudSesion;
import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Evento emitido cuando un tutor responde a una solicitud de sesión.
 */
@Getter
public class RespuestaSolicitudEvent extends NotificacionEvent {

    private final Long solicitudId;
    private final Integer tutorId;
    private final String nombreTutor;
    private final String estado;
    private final String notasTutor;

    public RespuestaSolicitudEvent(
            Object source,
            Integer estudianteId,
            Long solicitudId,
            Integer tutorId,
            String nombreTutor,
            String estado,
            String notasTutor) {
        super(
            source,
            estudianteId,
            TipoNotificacion.RESPUESTA_SOLICITUD,
            calcularPrioridad(estado),
            generarTitulo(estado),
            generarMensaje(nombreTutor, estado),
            crearDatosAdicionales(solicitudId, tutorId, nombreTutor, estado, notasTutor)
        );
        this.solicitudId = solicitudId;
        this.tutorId = tutorId;
        this.nombreTutor = nombreTutor;
        this.estado = estado;
        this.notasTutor = notasTutor;
    }

    private static PrioridadNotificacion calcularPrioridad(String estado) {
        if (EstadoSolicitudSesion.ACEPTADA.name().equals(estado)) {
            return PrioridadNotificacion.SUCCESS;
        } else if (EstadoSolicitudSesion.RECHAZADA.name().equals(estado)) {
            return PrioridadNotificacion.ALERTA;
        }
        return PrioridadNotificacion.INFO;
    }

    private static String generarTitulo(String estado) {
        if (EstadoSolicitudSesion.ACEPTADA.name().equals(estado)) {
            return "Solicitud de sesión aceptada";
        } else if (EstadoSolicitudSesion.RECHAZADA.name().equals(estado)) {
            return "Solicitud de sesión rechazada";
        } else if (EstadoSolicitudSesion.COMPLETADA.name().equals(estado)) {
            return "Sesión completada";
        }
        return "Actualización de solicitud de sesión";
    }

    private static String generarMensaje(String nombreTutor, String estado) {
        if (EstadoSolicitudSesion.ACEPTADA.name().equals(estado)) {
            return "El tutor " + nombreTutor + " ha aceptado tu solicitud de sesión.";
        } else if (EstadoSolicitudSesion.RECHAZADA.name().equals(estado)) {
            return "El tutor " + nombreTutor + " ha rechazado tu solicitud de sesión.";
        } else if (EstadoSolicitudSesion.COMPLETADA.name().equals(estado)) {
            return "El tutor " + nombreTutor + " ha marcado la sesión como completada.";
        }
        return "El tutor " + nombreTutor + " ha actualizado tu solicitud de sesión.";
    }

    private static Map<String, Object> crearDatosAdicionales(
            Long solicitudId,
            Integer tutorId,
            String nombreTutor,
            String estado,
            String notasTutor) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("solicitudId", solicitudId);
        datos.put("tutorId", tutorId);
        datos.put("nombreTutor", nombreTutor);
        datos.put("estado", estado);
        if (notasTutor != null) {
            datos.put("notasTutor", notasTutor);
        }
        return datos;
    }
}
