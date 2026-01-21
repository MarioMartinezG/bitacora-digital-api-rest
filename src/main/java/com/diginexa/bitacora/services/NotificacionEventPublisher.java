package com.diginexa.bitacora.services;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.events.SolicitudSesionEvent;
import com.diginexa.bitacora.events.UmbralAlcanzadoEvent;
import com.diginexa.bitacora.events.VencimientoProximoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Servicio para publicar eventos de notificación.
 * Encapsula la lógica de creación y publicación de eventos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publica un evento de vencimiento próximo para un estudiante.
     */
    public void publicarVencimientoEstudiante(
            Integer estudianteId,
            String seccionCodigo,
            String nombreModulo,
            LocalDate fechaLimite,
            int diasRestantes,
            String estadoProgreso) {

        PrioridadNotificacion prioridad = VencimientoProximoEvent.calcularPrioridad(estadoProgreso, diasRestantes);

        VencimientoProximoEvent event = new VencimientoProximoEvent(
                this,
                estudianteId,
                seccionCodigo,
                nombreModulo,
                fechaLimite,
                diasRestantes,
                estadoProgreso,
                prioridad,
                null,  // No es para un tutor viendo a otro estudiante
                null
        );

        log.info("Publicando evento de vencimiento próximo para estudiante {} - sección {}",
                estudianteId, seccionCodigo);
        eventPublisher.publishEvent(event);
    }

    /**
     * Publica un evento de vencimiento próximo para un tutor sobre un estudiante.
     */
    public void publicarVencimientoTutor(
            Integer tutorId,
            Integer estudianteId,
            String nombreEstudiante,
            String seccionCodigo,
            String nombreModulo,
            LocalDate fechaLimite,
            int diasRestantes,
            String estadoProgreso) {

        PrioridadNotificacion prioridad = VencimientoProximoEvent.calcularPrioridad(estadoProgreso, diasRestantes);

        VencimientoProximoEvent event = new VencimientoProximoEvent(
                this,
                tutorId,
                seccionCodigo,
                nombreModulo,
                fechaLimite,
                diasRestantes,
                estadoProgreso,
                prioridad,
                estudianteId,
                nombreEstudiante
        );

        log.info("Publicando evento de vencimiento próximo para tutor {} sobre estudiante {}",
                tutorId, estudianteId);
        eventPublisher.publishEvent(event);
    }

    /**
     * Publica un evento de solicitud de sesión.
     */
    public void publicarSolicitudSesion(
            Integer tutorId,
            Long solicitudId,
            Integer estudianteId,
            String nombreEstudiante,
            String motivo) {

        SolicitudSesionEvent event = new SolicitudSesionEvent(
                this,
                tutorId,
                solicitudId,
                estudianteId,
                nombreEstudiante,
                motivo
        );

        log.info("Publicando evento de solicitud de sesión para tutor {} de estudiante {}",
                tutorId, estudianteId);
        eventPublisher.publishEvent(event);
    }

    /**
     * Publica un evento de umbral de progreso alcanzado.
     */
    public void publicarUmbralAlcanzado(
            Integer tutorId,
            Integer estudianteId,
            String nombreEstudiante,
            String seccionCodigo,
            String nombreModulo,
            Integer porcentajeAlcanzado) {

        UmbralAlcanzadoEvent event = new UmbralAlcanzadoEvent(
                this,
                tutorId,
                estudianteId,
                nombreEstudiante,
                seccionCodigo,
                nombreModulo,
                porcentajeAlcanzado
        );

        log.info("Publicando evento de umbral alcanzado para tutor {} - estudiante {} alcanzó {}%",
                tutorId, estudianteId, porcentajeAlcanzado);
        eventPublisher.publishEvent(event);
    }
}
