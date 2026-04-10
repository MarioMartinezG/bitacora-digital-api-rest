package com.diginexa.bitacora.events;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Evento emitido cuando el tutor aprueba la bitácora completa de un estudiante.
 * Va dirigido al coordinador para informarle que la bitácora ha sido revisada y aprobada.
 */
@Getter
public class BitacoraAprobadaEvent extends NotificacionEvent {

    private final Integer estudianteId;
    private final String nombreEstudiante;
    private final Integer tutorId;
    private final String nombreTutor;

    public BitacoraAprobadaEvent(
            Object source,
            Integer coordinadorId,
            Integer estudianteId,
            String nombreEstudiante,
            Integer tutorId,
            String nombreTutor) {
        super(
            source,
            coordinadorId,
            TipoNotificacion.BITACORA_APROBADA,
            PrioridadNotificacion.SUCCESS,
            generarTitulo(nombreEstudiante),
            generarMensaje(nombreEstudiante, nombreTutor),
            crearDatosAdicionales(estudianteId, nombreEstudiante, tutorId, nombreTutor)
        );
        this.estudianteId = estudianteId;
        this.nombreEstudiante = nombreEstudiante;
        this.tutorId = tutorId;
        this.nombreTutor = nombreTutor;
    }

    private static String generarTitulo(String nombreEstudiante) {
        return "✓ Bitácora aprobada: " + nombreEstudiante;
    }

    private static String generarMensaje(String nombreEstudiante, String nombreTutor) {
        return String.format(
            "El tutor %s ha aprobado la bitácora completa del docente %s. " +
            "Todos los módulos han sido revisados y marcados como completados.",
            nombreTutor, nombreEstudiante
        );
    }

    private static Map<String, Object> crearDatosAdicionales(
            Integer estudianteId, String nombreEstudiante,
            Integer tutorId, String nombreTutor) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("estudianteId", estudianteId);
        datos.put("nombreEstudiante", nombreEstudiante);
        datos.put("tutorId", tutorId);
        datos.put("nombreTutor", nombreTutor);
        return datos;
    }
}
