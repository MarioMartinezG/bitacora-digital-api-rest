package com.diginexa.bitacora.events;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Evento emitido cuando un estudiante tiene un progreso tan bajo que le resulta
 * imposible completar la bitácora antes de la fecha límite del último momento.
 * Este evento va dirigido al coordinador.
 */
@Getter
public class EstudianteEnRiesgoEvent extends NotificacionEvent {

    private final Integer estudianteId;
    private final String nombreEstudiante;
    private final int progresoActual;
    private final int diasRestantes;
    private final LocalDate fechaLimite;

    public EstudianteEnRiesgoEvent(
            Object source,
            Integer coordinadorId,
            Integer estudianteId,
            String nombreEstudiante,
            int progresoActual,
            int diasRestantes,
            LocalDate fechaLimite) {
        super(
            source,
            coordinadorId,
            TipoNotificacion.ESTUDIANTE_EN_RIESGO,
            PrioridadNotificacion.CRITICO,
            generarTitulo(nombreEstudiante),
            generarMensaje(nombreEstudiante, progresoActual, diasRestantes, fechaLimite),
            crearDatosAdicionales(estudianteId, nombreEstudiante, progresoActual, diasRestantes, fechaLimite)
        );
        this.estudianteId = estudianteId;
        this.nombreEstudiante = nombreEstudiante;
        this.progresoActual = progresoActual;
        this.diasRestantes = diasRestantes;
        this.fechaLimite = fechaLimite;
    }

    private static String generarTitulo(String nombreEstudiante) {
        return "⚠ Estudiante en riesgo: " + nombreEstudiante;
    }

    private static String generarMensaje(String nombreEstudiante, int progresoActual, int diasRestantes, LocalDate fechaLimite) {
        return String.format(
            "El estudiante %s tiene un avance del %d%% y solo quedan %d día(s) hasta el vencimiento (%s). " +
            "Con el ritmo de trabajo actual, no es posible completar la bitácora a tiempo.",
            nombreEstudiante, progresoActual, diasRestantes, fechaLimite.toString()
        );
    }

    private static Map<String, Object> crearDatosAdicionales(
            Integer estudianteId, String nombreEstudiante,
            int progresoActual, int diasRestantes, LocalDate fechaLimite) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("estudianteId", estudianteId);
        datos.put("nombreEstudiante", nombreEstudiante);
        datos.put("progresoActual", progresoActual);
        datos.put("diasRestantes", diasRestantes);
        datos.put("fechaLimite", fechaLimite.toString());
        return datos;
    }
}
