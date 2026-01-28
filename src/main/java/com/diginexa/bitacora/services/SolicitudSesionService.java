package com.diginexa.bitacora.services;

import com.diginexa.bitacora.constants.EstadoSolicitudSesion;
import com.diginexa.bitacora.dtos.notificacion.CrearSolicitudSesionRequest;
import com.diginexa.bitacora.dtos.notificacion.ResponderSolicitudRequest;
import com.diginexa.bitacora.dtos.notificacion.SolicitudSesionDTO;
import com.diginexa.bitacora.entities.SolicitudSesion;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.exceptions.domain.SolicitudSesionNotFoundException;
import com.diginexa.bitacora.exceptions.domain.TutorNoAsignadoException;
import com.diginexa.bitacora.exceptions.validation.ForbiddenException;
import com.diginexa.bitacora.repositories.SolicitudSesionRepository;
import com.diginexa.bitacora.repositories.TutorEstudianteRepository;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SolicitudSesionService {

    private final SolicitudSesionRepository repository;
    private final TutorEstudianteRepository tutorEstudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionEventPublisher eventPublisher;

    public SolicitudSesionDTO crearSolicitud(CrearSolicitudSesionRequest request) {
        log.info("Creando solicitud de sesión para estudiante: {}", request.getEstudianteId());

        // Validar que el estudiante existe
        Usuario estudiante = usuarioRepository.findById(request.getEstudianteId())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con ID: " + request.getEstudianteId()));

        // Obtener el tutor asignado
        Usuario tutor = tutorEstudianteRepository.findTutorByEstudianteId(request.getEstudianteId())
                .orElseThrow(() -> new TutorNoAsignadoException(request.getEstudianteId()));

        // Verificar si ya existe una solicitud pendiente
        if (repository.existsByEstudianteIdAndTutorIdAndEstado(
                request.getEstudianteId(), tutor.getId(), EstadoSolicitudSesion.PENDIENTE.name())) {
            throw new IllegalStateException("Ya existe una solicitud de sesión pendiente con este tutor");
        }

        // Crear la solicitud
        SolicitudSesion solicitud = SolicitudSesion.builder()
                .estudianteId(request.getEstudianteId())
                .tutorId(tutor.getId())
                .motivo(request.getMotivo())
                .estado(EstadoSolicitudSesion.PENDIENTE.name())
                .build();

        SolicitudSesion guardada = repository.save(solicitud);
        log.info("Solicitud de sesión creada con ID: {}", guardada.getId());

        // Notificar al tutor
        eventPublisher.publicarSolicitudSesion(
                tutor.getId(),
                guardada.getId(),
                estudiante.getId(),
                estudiante.getNombre(),
                request.getMotivo()
        );

        return convertToDTO(guardada, estudiante, tutor);
    }

    @Transactional(readOnly = true)
    public List<SolicitudSesionDTO> obtenerPorEstudiante(Integer estudianteId) {
        return repository.findByEstudianteIdOrderByFechaSolicitudDesc(estudianteId)
                .stream()
                .map(this::convertToDTOWithRelations)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SolicitudSesionDTO> obtenerPorTutor(Integer tutorId) {
        return repository.findByTutorIdOrderByFechaSolicitudDesc(tutorId)
                .stream()
                .map(this::convertToDTOWithRelations)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SolicitudSesionDTO> obtenerPendientesPorTutor(Integer tutorId) {
        return repository.findByTutorIdAndEstadoOrderByFechaSolicitudDesc(
                        tutorId, EstadoSolicitudSesion.PENDIENTE.name())
                .stream()
                .map(this::convertToDTOWithRelations)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SolicitudSesionDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(this::convertToDTOWithRelations)
                .orElseThrow(() -> new SolicitudSesionNotFoundException(id));
    }

    public SolicitudSesionDTO responderSolicitud(Long id, ResponderSolicitudRequest request) {
        log.info("Respondiendo solicitud {} con estado: {}", id, request.getEstado());

        SolicitudSesion solicitud = repository.findById(id)
                .orElseThrow(() -> new SolicitudSesionNotFoundException(id));

        // Validar que el estado es válido
        try {
            EstadoSolicitudSesion nuevoEstado = EstadoSolicitudSesion.valueOf(request.getEstado());
            if (nuevoEstado != EstadoSolicitudSesion.ACEPTADA &&
                nuevoEstado != EstadoSolicitudSesion.RECHAZADA &&
                nuevoEstado != EstadoSolicitudSesion.COMPLETADA) {
                throw new IllegalArgumentException("Estado no válido para respuesta: " + request.getEstado());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado no válido: " + request.getEstado());
        }

        solicitud.setEstado(request.getEstado());
        solicitud.setFechaRespuesta(LocalDateTime.now());
        if (request.getNotasTutor() != null) {
            solicitud.setNotasTutor(request.getNotasTutor());
        }

        SolicitudSesion guardada = repository.save(solicitud);
        log.info("Solicitud {} actualizada a estado: {}", id, request.getEstado());

        // Notificar al estudiante sobre la respuesta
        Usuario tutor = usuarioRepository.findById(guardada.getTutorId()).orElse(null);
        String nombreTutor = tutor != null ? tutor.getNombre() : "Tu tutor";

        eventPublisher.publicarRespuestaSolicitud(
                guardada.getEstudianteId(),
                guardada.getId(),
                guardada.getTutorId(),
                nombreTutor,
                request.getEstado(),
                request.getNotasTutor()
        );

        return convertToDTOWithRelations(guardada);
    }

    public void cancelarSolicitud(Long id, Integer estudianteId) {
        SolicitudSesion solicitud = repository.findById(id)
                .orElseThrow(() -> new SolicitudSesionNotFoundException(id));

        if (!solicitud.getEstudianteId().equals(estudianteId)) {
            throw new ForbiddenException("No tienes permiso para cancelar esta solicitud");
        }

        if (!EstadoSolicitudSesion.PENDIENTE.name().equals(solicitud.getEstado())) {
            throw new IllegalStateException("Solo se pueden cancelar solicitudes pendientes");
        }

        repository.deleteById(id);
        log.info("Solicitud {} cancelada por estudiante {}", id, estudianteId);
    }

    private SolicitudSesionDTO convertToDTOWithRelations(SolicitudSesion entity) {
        Usuario estudiante = usuarioRepository.findById(entity.getEstudianteId()).orElse(null);
        Usuario tutor = usuarioRepository.findById(entity.getTutorId()).orElse(null);
        return convertToDTO(entity, estudiante, tutor);
    }

    private SolicitudSesionDTO convertToDTO(SolicitudSesion entity, Usuario estudiante, Usuario tutor) {
        return SolicitudSesionDTO.builder()
                .id(entity.getId())
                .estudianteId(entity.getEstudianteId())
                .nombreEstudiante(estudiante != null ? estudiante.getNombre() : null)
                .correoEstudiante(estudiante != null ? estudiante.getCorreo() : null)
                .tutorId(entity.getTutorId())
                .nombreTutor(tutor != null ? tutor.getNombre() : null)
                .correoTutor(tutor != null ? tutor.getCorreo() : null)
                .motivo(entity.getMotivo())
                .estado(entity.getEstado())
                .fechaSolicitud(entity.getFechaSolicitud())
                .fechaRespuesta(entity.getFechaRespuesta())
                .notasTutor(entity.getNotasTutor())
                .build();
    }
}
