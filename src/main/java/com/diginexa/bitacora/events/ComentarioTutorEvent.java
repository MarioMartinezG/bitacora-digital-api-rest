package com.diginexa.bitacora.events;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ComentarioTutorEvent extends NotificacionEvent {

    private final Integer tutorId;
    private final String nombreTutor;
    private final String seccionCodigo;
    private final String subseccionCodigo;

    public ComentarioTutorEvent(
            Object source,
            Integer estudianteId,
            Integer tutorId,
            String nombreTutor,
            String seccionCodigo,
            String subseccionCodigo) {
        super(
            source,
            estudianteId,
            TipoNotificacion.COMENTARIO_TUTOR,
            PrioridadNotificacion.INFO,
            "Nuevo comentario del tutor",
            "El tutor " + nombreTutor + " ha dejado un comentario en la sección " + seccionCodigo + ".",
            crearDatosAdicionales(tutorId, nombreTutor, seccionCodigo, subseccionCodigo)
        );
        this.tutorId = tutorId;
        this.nombreTutor = nombreTutor;
        this.seccionCodigo = seccionCodigo;
        this.subseccionCodigo = subseccionCodigo;
    }

    private static Map<String, Object> crearDatosAdicionales(
            Integer tutorId,
            String nombreTutor,
            String seccionCodigo,
            String subseccionCodigo) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("tutorId", tutorId);
        datos.put("nombreTutor", nombreTutor);
        datos.put("seccionCodigo", seccionCodigo);
        datos.put("subseccionCodigo", subseccionCodigo);
        return datos;
    }
}
