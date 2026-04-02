package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.ActualizarEstadoTutorSubseccionRequest;
import com.diginexa.bitacora.dtos.bitacora.EstadoTutorSubseccionDTO;
import com.diginexa.bitacora.entities.EstadoTutorSubseccion;
import com.diginexa.bitacora.entities.ProgresoSeccion;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.events.EstadoTutorActualizadoEvent;
import com.diginexa.bitacora.exceptions.domain.TutorNoAsignadoException;
import com.diginexa.bitacora.repositories.EstadoTutorSubseccionRepository;
import com.diginexa.bitacora.repositories.ProgresoSeccionRepository;
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
    private final ProgresoSeccionRepository progresoSeccionRepository;
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

        // Propagar el peor estado de subsección al estadoProfesor de la sección
        propagarEstadoSeccion(request.getEstudianteId(), request.getSeccionCodigo());

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

    /**
     * Recalcula el estadoProfesor de la sección completa según el "peor" estado
     * entre todas sus subsecciones evaluadas por el tutor:
     *   cualquier 'sin_avances'  → sección = sin_avances
     *   cualquier 'en_desarrollo' → sección = en_desarrollo
     *   todas 'completado'        → sección = completado
     */
    private void propagarEstadoSeccion(Integer estudianteId, String seccionCodigo) {
        List<EstadoTutorSubseccion> subsecciones =
                repository.findByEstudianteIdAndSeccionCodigo(estudianteId, seccionCodigo);

        if (subsecciones.isEmpty()) return;

        String estadoSeccion;
        boolean todasSinAvances = subsecciones.stream()
                .allMatch(s -> "sin_avances".equals(s.getEstado()));
        boolean todasCompletado = subsecciones.stream()
                .allMatch(s -> "completado".equals(s.getEstado()));

        if (todasSinAvances) {
            estadoSeccion = "sin_avances";
        } else if (todasCompletado) {
            estadoSeccion = "completado";
        } else {
            estadoSeccion = "en_desarrollo";
        }

        ProgresoSeccion progreso = progresoSeccionRepository
                .findByUsuarioIdAndSeccionCodigo(estudianteId, seccionCodigo)
                .orElse(ProgresoSeccion.builder()
                        .usuarioId(estudianteId)
                        .seccionCodigo(seccionCodigo)
                        .estado("sin_avances")
                        .porcentajeCompletado(0)
                        .build());

        progreso.setEstadoProfesor(estadoSeccion);
        progresoSeccionRepository.save(progreso);

        log.info("estadoProfesor de sección {}/{} propagado a '{}'",
                estudianteId, seccionCodigo, estadoSeccion);
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
