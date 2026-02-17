package com.diginexa.bitacora.events;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class EstadoTutorActualizadoEvent extends NotificacionEvent {

    private final Integer tutorId;
    private final String nombreTutor;
    private final String seccionCodigo;
    private final String subseccionCodigo;
    private final String nuevoEstado;

    public EstadoTutorActualizadoEvent(
            Object source,
            Integer estudianteId,
            Integer tutorId,
            String nombreTutor,
            String seccionCodigo,
            String subseccionCodigo,
            String nuevoEstado) {
        super(
            source,
            estudianteId,
            TipoNotificacion.ESTADO_TUTOR_ACTUALIZADO,
            PrioridadNotificacion.INFO,
            "El tutor actualizó el estado de una sección",
            "El tutor " + nombreTutor + " ha marcado la sub-sección " + subseccionCodigo
                + " de " + seccionCodigo + " como '" + nuevoEstado + "'.",
            crearDatosAdicionales(tutorId, nombreTutor, seccionCodigo, subseccionCodigo, nuevoEstado)
        );
        this.tutorId = tutorId;
        this.nombreTutor = nombreTutor;
        this.seccionCodigo = seccionCodigo;
        this.subseccionCodigo = subseccionCodigo;
        this.nuevoEstado = nuevoEstado;
    }

    private static Map<String, Object> crearDatosAdicionales(
            Integer tutorId,
            String nombreTutor,
            String seccionCodigo,
            String subseccionCodigo,
            String nuevoEstado) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("tutorId", tutorId);
        datos.put("nombreTutor", nombreTutor);
        datos.put("seccionCodigo", seccionCodigo);
        datos.put("subseccionCodigo", subseccionCodigo);
        datos.put("nuevoEstado", nuevoEstado);
        return datos;
    }
}
