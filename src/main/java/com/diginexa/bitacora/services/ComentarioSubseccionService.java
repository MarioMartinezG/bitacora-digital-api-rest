package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.ComentarioSubseccionDTO;
import com.diginexa.bitacora.dtos.bitacora.CrearComentarioSubseccionRequest;
import com.diginexa.bitacora.entities.ComentarioSubseccion;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.events.ComentarioResueltoPorEstudianteEvent;
import com.diginexa.bitacora.events.ComentarioTutorEvent;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.exceptions.domain.TutorNoAsignadoException;
import com.diginexa.bitacora.exceptions.security.UnauthorizedException;
import com.diginexa.bitacora.repositories.ComentarioSubseccionRepository;
import com.diginexa.bitacora.repositories.TutorEstudianteRepository;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ComentarioSubseccionService {

    private final ComentarioSubseccionRepository repository;
    private final TutorEstudianteRepository tutorEstudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ComentarioSubseccionDTO crearComentario(Integer tutorId, CrearComentarioSubseccionRequest request) {
        // Validar que el tutor tiene asignado al estudiante
        if (!tutorEstudianteRepository.existsByTutorIdAndEstudianteIdAndActivoTrue(tutorId, request.getEstudianteId())) {
            throw new TutorNoAsignadoException("El tutor no tiene asignado a este estudiante");
        }

        ComentarioSubseccion comentario = ComentarioSubseccion.builder()
                .tutorId(tutorId)
                .estudianteId(request.getEstudianteId())
                .seccionCodigo(request.getSeccionCodigo())
                .subseccionCodigo(request.getSubseccionCodigo())
                .comentario(request.getComentario())
                .build();

        ComentarioSubseccion guardado = repository.save(comentario);
        log.info("Comentario creado por tutor {} para estudiante {} en {}/{}",
                tutorId, request.getEstudianteId(), request.getSeccionCodigo(), request.getSubseccionCodigo());

        // Publicar evento para notificar al estudiante
        String nombreTutor = usuarioRepository.findById(tutorId)
                .map(Usuario::getNombre)
                .orElse("Tutor");

        eventPublisher.publishEvent(new ComentarioTutorEvent(
                this,
                request.getEstudianteId(),
                tutorId,
                nombreTutor,
                request.getSeccionCodigo(),
                request.getSubseccionCodigo()
        ));

        return convertToDTO(guardado, nombreTutor);
    }

    @Transactional(readOnly = true)
    public List<ComentarioSubseccionDTO> obtenerComentarios(
            Integer estudianteId, String seccionCodigo, String subseccionCodigo) {
        return repository.findByEstudianteIdAndSeccionCodigoAndSubseccionCodigoOrderByFechaCreacionDesc(
                estudianteId, seccionCodigo, subseccionCodigo)
                .stream()
                .map(c -> convertToDTO(c, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComentarioSubseccionDTO> obtenerComentariosPorSeccion(
            Integer estudianteId, String seccionCodigo) {
        return repository.findByEstudianteIdAndSeccionCodigoOrderByFechaCreacionDesc(
                estudianteId, seccionCodigo)
                .stream()
                .map(c -> convertToDTO(c, null))
                .collect(Collectors.toList());
    }

    public ComentarioSubseccionDTO toggleResuelto(Long comentarioId, Integer estudianteId) {
        ComentarioSubseccion comentario = repository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con id: " + comentarioId));

        if (!comentario.getEstudianteId().equals(estudianteId)) {
            throw new UnauthorizedException("No tienes permiso para modificar este comentario");
        }

        boolean nuevoEstado = !comentario.isResuelto();
        comentario.setResuelto(nuevoEstado);
        comentario.setFechaResolucion(nuevoEstado ? LocalDateTime.now() : null);

        ComentarioSubseccion guardado = repository.save(comentario);
        log.info("Comentario {} marcado como {} por estudiante {}", comentarioId, nuevoEstado ? "resuelto" : "no resuelto", estudianteId);

        if (nuevoEstado) {
            String nombreEstudiante = usuarioRepository.findById(estudianteId)
                    .map(Usuario::getNombre)
                    .orElse("El estudiante");

            eventPublisher.publishEvent(new ComentarioResueltoPorEstudianteEvent(
                    this,
                    comentario.getTutorId(),
                    nombreEstudiante,
                    comentario.getSeccionCodigo(),
                    comentario.getSubseccionCodigo()
            ));
        }

        return convertToDTO(guardado, null);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> contarComentariosPorSeccion(Integer estudianteId, String seccionCodigo) {
        List<ComentarioSubseccion> comentarios = repository
                .findByEstudianteIdAndSeccionCodigoOrderByFechaCreacionDesc(estudianteId, seccionCodigo);

        return comentarios.stream()
                .collect(Collectors.groupingBy(
                        ComentarioSubseccion::getSubseccionCodigo,
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    private ComentarioSubseccionDTO convertToDTO(ComentarioSubseccion entity, String nombreTutor) {
        if (nombreTutor == null && entity.getTutor() != null) {
            nombreTutor = entity.getTutor().getNombre();
        }
        if (nombreTutor == null) {
            nombreTutor = usuarioRepository.findById(entity.getTutorId())
                    .map(Usuario::getNombre)
                    .orElse("Tutor");
        }

        return ComentarioSubseccionDTO.builder()
                .id(entity.getId())
                .tutorId(entity.getTutorId())
                .nombreTutor(nombreTutor)
                .estudianteId(entity.getEstudianteId())
                .seccionCodigo(entity.getSeccionCodigo())
                .subseccionCodigo(entity.getSubseccionCodigo())
                .comentario(entity.getComentario())
                .fechaCreacion(entity.getFechaCreacion())
                .resuelto(entity.isResuelto())
                .fechaResolucion(entity.getFechaResolucion())
                .build();
    }
}
