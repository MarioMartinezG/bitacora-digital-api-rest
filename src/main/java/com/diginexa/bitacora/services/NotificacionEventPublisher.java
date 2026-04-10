package com.diginexa.bitacora.services;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import com.diginexa.bitacora.events.*;
import com.diginexa.bitacora.events.BitacoraAprobadaEstudianteEvent;
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

    /**
     * Publica un evento de respuesta a solicitud de sesión.
     */
    public void publicarRespuestaSolicitud(
            Integer estudianteId,
            Long solicitudId,
            Integer tutorId,
            String nombreTutor,
            String estado,
            String notasTutor) {

        RespuestaSolicitudEvent event = new RespuestaSolicitudEvent(
                this,
                estudianteId,
                solicitudId,
                tutorId,
                nombreTutor,
                estado,
                notasTutor
        );

        log.info("Publicando evento de respuesta a solicitud {} para estudiante {} - estado: {}",
                solicitudId, estudianteId, estado);
        eventPublisher.publishEvent(event);
    }

    /**
     * Publica un evento de estudiante en riesgo dirigido a un coordinador.
     */
    public void publicarEstudianteEnRiesgo(
            Integer coordinadorId,
            Integer estudianteId,
            String nombreEstudiante,
            int progresoActual,
            int diasRestantes,
            LocalDate fechaLimite) {

        EstudianteEnRiesgoEvent event = new EstudianteEnRiesgoEvent(
                this,
                coordinadorId,
                estudianteId,
                nombreEstudiante,
                progresoActual,
                diasRestantes,
                fechaLimite
        );

        log.info("Publicando evento de estudiante en riesgo: coordinador={}, estudiante={} ({}%), {} días restantes",
                coordinadorId, estudianteId, progresoActual, diasRestantes);
        eventPublisher.publishEvent(event);
    }

    /**
     * Publica un evento de bitácora aprobada dirigido a un coordinador.
     */
    public void publicarBitacoraAprobada(
            Integer coordinadorId,
            Integer estudianteId,
            String nombreEstudiante,
            Integer tutorId,
            String nombreTutor) {

        BitacoraAprobadaEvent event = new BitacoraAprobadaEvent(
                this,
                coordinadorId,
                estudianteId,
                nombreEstudiante,
                tutorId,
                nombreTutor
        );

        log.info("Publicando evento de bitácora aprobada: coordinador={}, estudiante={}, tutor={}",
                coordinadorId, estudianteId, tutorId);
        eventPublisher.publishEvent(event);
    }

    /**
     * Publica un evento de bitácora aprobada dirigido al propio estudiante.
     */
    public void publicarBitacoraAprobadaAEstudiante(
            Integer estudianteId,
            Integer tutorId,
            String nombreTutor) {

        BitacoraAprobadaEstudianteEvent event = new BitacoraAprobadaEstudianteEvent(
                this,
                estudianteId,
                tutorId,
                nombreTutor
        );

        log.info("Publicando evento de bitácora aprobada para estudiante={}, tutor={}",
                estudianteId, tutorId);
        eventPublisher.publishEvent(event);
    }

    /**
     * Publica un evento de comentario del tutor en una sub-sección.
     */
    public void publicarComentarioTutor(
            Integer estudianteId,
            Integer tutorId,
            String nombreTutor,
            String seccionCodigo,
            String subseccionCodigo) {

        ComentarioTutorEvent event = new ComentarioTutorEvent(
                this,
                estudianteId,
                tutorId,
                nombreTutor,
                seccionCodigo,
                subseccionCodigo
        );

        log.info("Publicando evento de comentario tutor {} para estudiante {} en {}/{}",
                tutorId, estudianteId, seccionCodigo, subseccionCodigo);
        eventPublisher.publishEvent(event);
    }
}
