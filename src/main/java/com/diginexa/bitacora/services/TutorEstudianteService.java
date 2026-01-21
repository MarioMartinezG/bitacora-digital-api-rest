package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.notificacion.AsignarTutorRequest;
import com.diginexa.bitacora.dtos.notificacion.TutorEstudianteDTO;
import com.diginexa.bitacora.entities.TutorEstudiante;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.exceptions.domain.TutorNoAsignadoException;
import com.diginexa.bitacora.repositories.TutorEstudianteRepository;
import com.diginexa.bitacora.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TutorEstudianteService {

    private final TutorEstudianteRepository repository;
    private final UsuarioRepository usuarioRepository;

    public TutorEstudianteDTO asignarTutor(AsignarTutorRequest request) {
        log.info("Asignando tutor {} a estudiante {}", request.getTutorId(), request.getEstudianteId());

        // Validar que el tutor existe
        Usuario tutor = usuarioRepository.findById(request.getTutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor no encontrado con ID: " + request.getTutorId()));

        // Validar que el estudiante existe
        Usuario estudiante = usuarioRepository.findById(request.getEstudianteId())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado con ID: " + request.getEstudianteId()));

        // Verificar si ya existe una asignación activa para este estudiante
        Optional<TutorEstudiante> asignacionExistente = repository
                .findByEstudianteIdAndActivoTrue(request.getEstudianteId());

        if (asignacionExistente.isPresent()) {
            // Desactivar la asignación anterior
            TutorEstudiante anterior = asignacionExistente.get();
            anterior.setActivo(false);
            repository.save(anterior);
            log.info("Asignación anterior del estudiante {} desactivada", request.getEstudianteId());
        }

        // Crear nueva asignación
        TutorEstudiante nuevaAsignacion = TutorEstudiante.builder()
                .tutorId(request.getTutorId())
                .estudianteId(request.getEstudianteId())
                .activo(true)
                .build();

        TutorEstudiante guardada = repository.save(nuevaAsignacion);
        log.info("Nueva asignación creada: tutor {} -> estudiante {}", request.getTutorId(), request.getEstudianteId());

        return convertToDTO(guardada, tutor, estudiante);
    }

    @Transactional(readOnly = true)
    public List<TutorEstudianteDTO> obtenerEstudiantesPorTutor(Integer tutorId) {
        log.debug("Obteniendo estudiantes asignados al tutor {}", tutorId);

        return repository.findByTutorIdAndActivoTrue(tutorId)
                .stream()
                .map(this::convertToDTOWithRelations)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TutorEstudianteDTO obtenerTutorPorEstudiante(Integer estudianteId) {
        log.debug("Obteniendo tutor asignado al estudiante {}", estudianteId);

        TutorEstudiante asignacion = repository.findByEstudianteIdAndActivoTrue(estudianteId)
                .orElseThrow(() -> new TutorNoAsignadoException(estudianteId));

        return convertToDTOWithRelations(asignacion);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerTutorEntityPorEstudiante(Integer estudianteId) {
        return repository.findTutorByEstudianteId(estudianteId);
    }

    @Transactional(readOnly = true)
    public boolean tieneTutorAsignado(Integer estudianteId) {
        return repository.existsByEstudianteIdAndActivoTrue(estudianteId);
    }

    public void desactivarAsignacion(Long asignacionId) {
        TutorEstudiante asignacion = repository.findById(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + asignacionId));

        asignacion.setActivo(false);
        repository.save(asignacion);
        log.info("Asignación {} desactivada", asignacionId);
    }

    private TutorEstudianteDTO convertToDTOWithRelations(TutorEstudiante entity) {
        Usuario tutor = usuarioRepository.findById(entity.getTutorId()).orElse(null);
        Usuario estudiante = usuarioRepository.findById(entity.getEstudianteId()).orElse(null);
        return convertToDTO(entity, tutor, estudiante);
    }

    private TutorEstudianteDTO convertToDTO(TutorEstudiante entity, Usuario tutor, Usuario estudiante) {
        return TutorEstudianteDTO.builder()
                .id(entity.getId())
                .tutorId(entity.getTutorId())
                .nombreTutor(tutor != null ? tutor.getNombre() : null)
                .correoTutor(tutor != null ? tutor.getCorreo() : null)
                .estudianteId(entity.getEstudianteId())
                .nombreEstudiante(estudiante != null ? estudiante.getNombre() : null)
                .correoEstudiante(estudiante != null ? estudiante.getCorreo() : null)
                .fechaAsignacion(entity.getFechaAsignacion())
                .activo(entity.getActivo())
                .build();
    }
}
