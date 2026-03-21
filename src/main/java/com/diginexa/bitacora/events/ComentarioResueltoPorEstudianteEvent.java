package com.diginexa.bitacora.events;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;

import java.util.HashMap;
import java.util.Map;

public class ComentarioResueltoPorEstudianteEvent extends NotificacionEvent {

    public ComentarioResueltoPorEstudianteEvent(
            Object source,
            Integer tutorId,
            String nombreEstudiante,
            String seccionCodigo,
            String subseccionCodigo) {
        super(
            source,
            tutorId,
            TipoNotificacion.COMENTARIO_RESUELTO,
            PrioridadNotificacion.INFO,
            "Comentario resuelto por el estudiante",
            "El estudiante " + nombreEstudiante + " marcó como resuelto un comentario en la sección " + seccionCodigo + ".",
            crearDatosAdicionales(nombreEstudiante, seccionCodigo, subseccionCodigo)
        );
    }

    private static Map<String, Object> crearDatosAdicionales(
            String nombreEstudiante,
            String seccionCodigo,
            String subseccionCodigo) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombreEstudiante", nombreEstudiante);
        datos.put("seccionCodigo", seccionCodigo);
        datos.put("subseccionCodigo", subseccionCodigo);
        return datos;
    }
}
