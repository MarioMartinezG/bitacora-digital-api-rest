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

import java.util.ArrayList;
import java.util.Collections;
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

        // Reusar registro existente (activo o inactivo) para evitar violar la unique constraint
        TutorEstudiante asignacion = repository
                .findByTutorIdAndEstudianteId(request.getTutorId(), request.getEstudianteId())
                .orElse(TutorEstudiante.builder()
                        .tutorId(request.getTutorId())
                        .estudianteId(request.getEstudianteId())
                        .build());
        asignacion.setActivo(true);

        TutorEstudiante guardada = repository.save(asignacion);
        log.info("Asignación guardada: tutor {} -> estudiante {}", request.getTutorId(), request.getEstudianteId());

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

    @Transactional(readOnly = true)
    public List<TutorEstudianteDTO> obtenerTodasAsignaciones() {
        return repository.findAllByActivoTrue()
                .stream()
                .map(this::convertToDTOWithRelations)
                .collect(Collectors.toList());
    }

    public List<TutorEstudianteDTO> asignarAleatorio() {
        List<Usuario> tutores = usuarioRepository.findByRolNombre("tutor");
        List<Usuario> estudiantes = usuarioRepository.findByRolNombre("estudiante").stream()
                .filter(e -> !Boolean.TRUE.equals(e.getGraduado()))
                .collect(Collectors.toList());

        if (tutores.isEmpty()) {
            throw new IllegalStateException("No hay tutores activos para realizar la asignación");
        }
        if (estudiantes.isEmpty()) {
            throw new IllegalStateException("No hay estudiantes activos para asignar");
        }

        // Desactivar todas las asignaciones existentes
        repository.findAllByActivoTrue().forEach(a -> {
            a.setActivo(false);
            repository.save(a);
        });

        // Distribuir estudiantes de forma equitativa (round-robin)
        Collections.shuffle(estudiantes);
        List<TutorEstudianteDTO> resultado = new ArrayList<>();
        for (int i = 0; i < estudiantes.size(); i++) {
            Usuario tutor = tutores.get(i % tutores.size());
            Usuario estudiante = estudiantes.get(i);

            TutorEstudiante nueva = repository
                    .findByTutorIdAndEstudianteId(tutor.getId(), estudiante.getId())
                    .orElse(TutorEstudiante.builder()
                            .tutorId(tutor.getId())
                            .estudianteId(estudiante.getId())
                            .build());
            nueva.setActivo(true);
            TutorEstudiante guardada = repository.save(nueva);
            resultado.add(convertToDTO(guardada, tutor, estudiante));
        }

        log.info("Asignación aleatoria completada: {} estudiantes distribuidos entre {} tutores",
                estudiantes.size(), tutores.size());
        return resultado;
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
