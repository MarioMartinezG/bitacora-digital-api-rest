package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.ActualizarEstadoTutorSubseccionRequest;
import com.diginexa.bitacora.dtos.bitacora.EstadoTutorSubseccionDTO;
import com.diginexa.bitacora.entities.EstadoTutorSubseccion;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.events.EstadoTutorActualizadoEvent;
import com.diginexa.bitacora.exceptions.domain.TutorNoAsignadoException;
import com.diginexa.bitacora.repositories.EstadoTutorSubseccionRepository;
import com.diginexa.bitacora.repositories.TutorEstudianteRepository;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EstadoTutorSubseccionService {

    private final EstadoTutorSubseccionRepository repository;
    private final TutorEstudianteRepository tutorEstudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EstadoTutorSubseccionDTO actualizarEstado(Integer tutorId, ActualizarEstadoTutorSubseccionRequest request) {
        if (!tutorEstudianteRepository.existsByTutorIdAndEstudianteIdAndActivoTrue(tutorId, request.getEstudianteId())) {
            throw new TutorNoAsignadoException("El tutor no tiene asignado a este estudiante");
        }

        EstadoTutorSubseccion estado = repository
                .findByEstudianteIdAndSeccionCodigoAndSubseccionCodigo(
                        request.getEstudianteId(), request.getSeccionCodigo(), request.getSubseccionCodigo())
                .orElse(EstadoTutorSubseccion.builder()
                        .tutorId(tutorId)
                        .estudianteId(request.getEstudianteId())
                        .seccionCodigo(request.getSeccionCodigo())
                        .subseccionCodigo(request.getSubseccionCodigo())
                        .build());

        estado.setEstado(request.getEstado());
        estado.setTutorId(tutorId);

        EstadoTutorSubseccion guardado = repository.save(estado);
        log.info("Estado tutor actualizado: estudiante={}, seccion={}/{}, estado={}",
                request.getEstudianteId(), request.getSeccionCodigo(), request.getSubseccionCodigo(), request.getEstado());

        // Publicar evento de notificación para el estudiante
        String nombreTutor = usuarioRepository.findById(tutorId)
                .map(Usuario::getNombre)
                .orElse("Tutor");

        eventPublisher.publishEvent(new EstadoTutorActualizadoEvent(
                this,
                request.getEstudianteId(),
                tutorId,
                nombreTutor,
                request.getSeccionCodigo(),
                request.getSubseccionCodigo(),
                request.getEstado()
        ));

        return convertToDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<EstadoTutorSubseccionDTO> obtenerEstadosPorSeccion(Integer estudianteId, String seccionCodigo) {
        return repository.findByEstudianteIdAndSeccionCodigo(estudianteId, seccionCodigo)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EstadoTutorSubseccionDTO> obtenerEstadosPorEstudiante(Integer estudianteId) {
        return repository.findByEstudianteId(estudianteId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EstadoTutorSubseccionDTO convertToDTO(EstadoTutorSubseccion entity) {
        return EstadoTutorSubseccionDTO.builder()
                .id(entity.getId())
                .seccionCodigo(entity.getSeccionCodigo())
                .subseccionCodigo(entity.getSubseccionCodigo())
                .estado(entity.getEstado())
                .fechaActualizacion(entity.getFechaActualizacion())
                .build();
    }
}
