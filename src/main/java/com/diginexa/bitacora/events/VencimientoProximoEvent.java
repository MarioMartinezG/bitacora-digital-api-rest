package com.diginexa.bitacora.events;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.constants.TipoNotificacion;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Evento emitido cuando se acerca el vencimiento de un módulo/sección.
 */
@Getter
public class VencimientoProximoEvent extends NotificacionEvent {

    private final String seccionCodigo;
    private final LocalDate fechaLimite;
    private final int diasRestantes;
    private final String estadoProgreso;
    private final Integer estudianteId;
    private final String nombreEstudiante;

    public VencimientoProximoEvent(
            Object source,
            Integer usuarioDestinoId,
            String seccionCodigo,
            String nombreModulo,
            LocalDate fechaLimite,
            int diasRestantes,
            String estadoProgreso,
            PrioridadNotificacion prioridad,
            Integer estudianteId,
            String nombreEstudiante) {
        super(
            source,
            usuarioDestinoId,
            TipoNotificacion.VENCIMIENTO_PROXIMO,
            prioridad,
            generarTitulo(diasRestantes, nombreModulo),
            generarMensaje(diasRestantes, nombreModulo, estadoProgreso, nombreEstudiante),
            crearDatosAdicionales(seccionCodigo, fechaLimite, diasRestantes, estadoProgreso, estudianteId, nombreEstudiante)
        );
        this.seccionCodigo = seccionCodigo;
        this.fechaLimite = fechaLimite;
        this.diasRestantes = diasRestantes;
        this.estadoProgreso = estadoProgreso;
        this.estudianteId = estudianteId;
        this.nombreEstudiante = nombreEstudiante;
    }

    private static String generarTitulo(int diasRestantes, String nombreModulo) {
        if (diasRestantes <= 0) {
            return "¡Plazo vencido! - " + nombreModulo;
        } else if (diasRestantes == 1) {
            return "¡Último día! - " + nombreModulo;
        } else {
            return diasRestantes + " días restantes - " + nombreModulo;
        }
    }

    private static String generarMensaje(int diasRestantes, String nombreModulo, String estadoProgreso, String nombreEstudiante) {
        StringBuilder sb = new StringBuilder();

        if (nombreEstudiante != null) {
            sb.append("El estudiante ").append(nombreEstudiante).append(" ");
        }

        if (diasRestantes <= 0) {
            sb.append("El plazo para completar \"").append(nombreModulo).append("\" ha vencido. ");
        } else if (diasRestantes == 1) {
            sb.append("tiene hasta mañana para completar \"").append(nombreModulo).append("\". ");
        } else {
            sb.append("tiene ").append(diasRestantes).append(" días para completar \"").append(nombreModulo).append("\". ");
        }

        sb.append("Estado actual: ").append(estadoProgreso.replace("_", " ")).append(".");

        return sb.toString();
    }

    private static Map<String, Object> crearDatosAdicionales(
            String seccionCodigo,
            LocalDate fechaLimite,
            int diasRestantes,
            String estadoProgreso,
            Integer estudianteId,
            String nombreEstudiante) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("seccionCodigo", seccionCodigo);
        datos.put("fechaLimite", fechaLimite.toString());
        datos.put("diasRestantes", diasRestantes);
        datos.put("estadoProgreso", estadoProgreso);
        if (estudianteId != null) {
            datos.put("estudianteId", estudianteId);
            datos.put("nombreEstudiante", nombreEstudiante);
        }
        return datos;
    }

    /**
     * Calcula la prioridad basada en el estado del progreso y días restantes.
     */
    public static PrioridadNotificacion calcularPrioridad(String estado, int diasRestantes) {
        if ("sin_avances".equals(estado) || diasRestantes <= 1) {
            return PrioridadNotificacion.CRITICO;
        } else if ("en_desarrollo".equals(estado) || diasRestantes <= 3) {
            return PrioridadNotificacion.ALERTA;
        }
        return PrioridadNotificacion.INFO;
    }
}
