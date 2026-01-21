package com.diginexa.bitacora.events;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Evento emitido cuando un estudiante alcanza un umbral de progreso configurado.
 */
@Getter
public class UmbralAlcanzadoEvent extends NotificacionEvent {

    private final Integer estudianteId;
    private final String nombreEstudiante;
    private final String seccionCodigo;
    private final String nombreModulo;
    private final Integer porcentajeAlcanzado;

    public UmbralAlcanzadoEvent(
            Object source,
            Integer tutorId,
            Integer estudianteId,
            String nombreEstudiante,
            String seccionCodigo,
            String nombreModulo,
            Integer porcentajeAlcanzado) {
        super(
            source,
            tutorId,
            TipoNotificacion.UMBRAL_ALCANZADO,
            PrioridadNotificacion.INFO,
            "Estudiante alcanzó umbral de progreso",
            generarMensaje(nombreEstudiante, nombreModulo, porcentajeAlcanzado),
            crearDatosAdicionales(estudianteId, nombreEstudiante, seccionCodigo, nombreModulo, porcentajeAlcanzado)
        );
        this.estudianteId = estudianteId;
        this.nombreEstudiante = nombreEstudiante;
        this.seccionCodigo = seccionCodigo;
        this.nombreModulo = nombreModulo;
        this.porcentajeAlcanzado = porcentajeAlcanzado;
    }

    private static String generarMensaje(String nombreEstudiante, String nombreModulo, Integer porcentaje) {
        return "El estudiante " + nombreEstudiante + " ha alcanzado el " + porcentaje +
               "% de progreso en \"" + nombreModulo + "\". Revisa sus respuestas.";
    }

    private static Map<String, Object> crearDatosAdicionales(
            Integer estudianteId,
            String nombreEstudiante,
            String seccionCodigo,
            String nombreModulo,
            Integer porcentajeAlcanzado) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("estudianteId", estudianteId);
        datos.put("nombreEstudiante", nombreEstudiante);
        datos.put("seccionCodigo", seccionCodigo);
        datos.put("nombreModulo", nombreModulo);
        datos.put("porcentajeAlcanzado", porcentajeAlcanzado);
        return datos;
    }
}
