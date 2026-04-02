package com.diginexa.bitacora.events;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Evento emitido cuando el tutor aprueba la bitácora completa de un estudiante.
 * Va dirigido al propio estudiante para informarle que su bitácora fue aprobada.
 */
@Getter
public class BitacoraAprobadaEstudianteEvent extends NotificacionEvent {

    private final Integer tutorId;
    private final String nombreTutor;

    public BitacoraAprobadaEstudianteEvent(
            Object source,
            Integer estudianteId,
            Integer tutorId,
            String nombreTutor) {
        super(
            source,
            estudianteId,
            TipoNotificacion.BITACORA_APROBADA,
            PrioridadNotificacion.SUCCESS,
            "¡Bitácora aprobada!",
            generarMensaje(nombreTutor),
            crearDatosAdicionales(tutorId, nombreTutor)
        );
        this.tutorId = tutorId;
        this.nombreTutor = nombreTutor;
    }

    private static String generarMensaje(String nombreTutor) {
        return String.format(
            "¡Tu tutor %s ha aprobado tu bitácora! " +
            "Todos los módulos han sido revisados y marcados como completados. ¡Felicitaciones!",
            nombreTutor
        );
    }

    private static Map<String, Object> crearDatosAdicionales(Integer tutorId, String nombreTutor) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("tutorId", tutorId);
        datos.put("nombreTutor", nombreTutor);
        return datos;
    }
}
