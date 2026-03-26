package com.diginexa.bitacora.services;

import com.diginexa.bitacora.constants.SeccionCodigos;
import com.diginexa.bitacora.dtos.bitacora.EstudianteProgresoResumenDTO;
import com.diginexa.bitacora.dtos.bitacora.MarcarRevisadoRequest;
import com.diginexa.bitacora.dtos.bitacora.ProgresoUsuarioDTO;
import com.diginexa.bitacora.entities.ProgresoSeccion;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.repositories.ProgresoSeccionRepository;
import com.diginexa.bitacora.repositories.TutorEstudianteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProgresoService {

    private final ProgresoSeccionRepository repository;
    private final TutorEstudianteRepository tutorEstudianteRepository;

    @Transactional(readOnly = true)
    public ProgresoUsuarioDTO obtenerProgresoCompleto(Integer usuarioId) {
        log.debug("Obteniendo progreso completo para usuario {}", usuarioId);

        List<ProgresoSeccion> progresos = repository.findByUsuarioId(usuarioId);

        Map<String, ProgresoUsuarioDTO.ProgresoSeccionDTO> secciones = new HashMap<>();
        int totalSecciones = SeccionCodigos.TODOS.length;

        // Inicializar todas las secciones con estado sin_avances
        for (String codigo : SeccionCodigos.TODOS) {
            secciones.put(codigo, ProgresoUsuarioDTO.ProgresoSeccionDTO.builder()
                .seccionCodigo(codigo)
                .estado("sin_avances")
                .porcentaje(0)
                .revisado(false)
                .build());
        }

        // Actualizar con datos reales (solo módulos activos)
        int sumaPorcentajes = 0;
        for (ProgresoSeccion p : progresos) {
            if (!SeccionCodigos.esValido(p.getSeccionCodigo())) {
                continue; // Ignorar módulos obsoletos (ajustes, rap-rac)
            }
            ProgresoUsuarioDTO.ProgresoSeccionDTO dto = ProgresoUsuarioDTO.ProgresoSeccionDTO.builder()
                .seccionCodigo(p.getSeccionCodigo())
                .estado(p.getEstado())
                .porcentaje(p.getPorcentajeCompletado())
                .estadoProfesor(p.getEstadoProfesor())
                .revisado(Boolean.TRUE.equals(p.getRevisado()))
                .build();
            secciones.put(p.getSeccionCodigo(), dto);
            sumaPorcentajes += p.getPorcentajeCompletado();
        }

        // Promediar porcentajes reales (mismo cálculo que el frontend)
        int porcentajeTotal = totalSecciones > 0
            ? Math.round((float) sumaPorcentajes / totalSecciones)
            : 0;

        String estadoGeneral = calcularEstadoDesdeProgreso(porcentajeTotal);

        return ProgresoUsuarioDTO.builder()
            .usuarioId(usuarioId)
            .secciones(secciones)
            .porcentajeTotal(porcentajeTotal)
            .estadoGeneral(estadoGeneral)
            .build();
    }

    public ProgresoUsuarioDTO.ProgresoSeccionDTO actualizarEstadoSeccion(
            Integer usuarioId,
            String seccionCodigo,
            String estado,
            Integer progresoPorcentaje) {
        log.info("Actualizando estado de sección {} a {} ({}%) para usuario {}",
                 seccionCodigo, estado, progresoPorcentaje, usuarioId);

        ProgresoSeccion progreso = repository
            .findByUsuarioIdAndSeccionCodigo(usuarioId, seccionCodigo)
            .orElse(ProgresoSeccion.builder()
                .usuarioId(usuarioId)
                .seccionCodigo(seccionCodigo)
                .build());

        progreso.setEstado(estado);
        progreso.setPorcentajeCompletado(
            progresoPorcentaje != null ? progresoPorcentaje : calcularPorcentaje(estado)
        );

        ProgresoSeccion guardado = repository.save(progreso);

        return ProgresoUsuarioDTO.ProgresoSeccionDTO.builder()
            .seccionCodigo(guardado.getSeccionCodigo())
            .estado(guardado.getEstado())
            .porcentaje(guardado.getPorcentajeCompletado())
            .estadoProfesor(guardado.getEstadoProfesor())
            .revisado(Boolean.TRUE.equals(guardado.getRevisado()))
            .build();
    }

    @Transactional(readOnly = true)
    public ProgresoUsuarioDTO.ProgresoSeccionDTO obtenerProgresoSeccion(
            Integer usuarioId,
            String seccionCodigo) {
        return repository.findByUsuarioIdAndSeccionCodigo(usuarioId, seccionCodigo)
            .map(p -> ProgresoUsuarioDTO.ProgresoSeccionDTO.builder()
                .seccionCodigo(p.getSeccionCodigo())
                .estado(p.getEstado())
                .porcentaje(p.getPorcentajeCompletado())
                .estadoProfesor(p.getEstadoProfesor())
                .revisado(Boolean.TRUE.equals(p.getRevisado()))
                .build())
            .orElse(ProgresoUsuarioDTO.ProgresoSeccionDTO.builder()
                .seccionCodigo(seccionCodigo)
                .estado("sin_avances")
                .porcentaje(0)
                .estadoProfesor(null)
                .revisado(false)
                .build());
    }

    /**
     * Actualiza el estado asignado por el profesor/tutor.
     * Este estado tiene prioridad sobre el estado calculado automáticamente.
     *
     * @param estudianteId ID del estudiante
     * @param seccionCodigo Código de la sección
     * @param estadoProfesor Nuevo estado asignado por el profesor (puede ser null para limpiar)
     * @return DTO con el progreso actualizado
     */
    public ProgresoUsuarioDTO.ProgresoSeccionDTO actualizarEstadoProfesor(
            Integer estudianteId,
            String seccionCodigo,
            String estadoProfesor) {
        log.info("Profesor actualizando estado de sección {} a '{}' para estudiante {}",
                 seccionCodigo, estadoProfesor, estudianteId);

        ProgresoSeccion progreso = repository
            .findByUsuarioIdAndSeccionCodigo(estudianteId, seccionCodigo)
            .orElse(ProgresoSeccion.builder()
                .usuarioId(estudianteId)
                .seccionCodigo(seccionCodigo)
                .estado("sin_avances")
                .porcentajeCompletado(0)
                .build());

        progreso.setEstadoProfesor(estadoProfesor);

        ProgresoSeccion guardado = repository.save(progreso);

        log.info("Estado de profesor actualizado exitosamente para sección {} del estudiante {}",
                 seccionCodigo, estudianteId);

        return ProgresoUsuarioDTO.ProgresoSeccionDTO.builder()
            .seccionCodigo(guardado.getSeccionCodigo())
            .estado(guardado.getEstado())
            .porcentaje(guardado.getPorcentajeCompletado())
            .estadoProfesor(guardado.getEstadoProfesor())
            .revisado(Boolean.TRUE.equals(guardado.getRevisado()))
            .build();
    }

    /**
     * Marca o desmarca una sección como revisada por el tutor.
     *
     * @param estudianteId  ID del estudiante
     * @param seccionCodigo Código de la sección
     * @param revisado      true para marcar como revisada, false para desmarcar
     * @return DTO con el progreso actualizado
     */
    public ProgresoUsuarioDTO.ProgresoSeccionDTO marcarRevisado(
            Integer estudianteId,
            String seccionCodigo,
            Boolean revisado) {
        log.info("Tutor marcando sección {} como revisado={} para estudiante {}",
                 seccionCodigo, revisado, estudianteId);

        ProgresoSeccion progreso = repository
            .findByUsuarioIdAndSeccionCodigo(estudianteId, seccionCodigo)
            .orElse(ProgresoSeccion.builder()
                .usuarioId(estudianteId)
                .seccionCodigo(seccionCodigo)
                .estado("sin_avances")
                .porcentajeCompletado(0)
                .build());

        progreso.setRevisado(revisado != null ? revisado : false);

        ProgresoSeccion guardado = repository.save(progreso);

        return ProgresoUsuarioDTO.ProgresoSeccionDTO.builder()
            .seccionCodigo(guardado.getSeccionCodigo())
            .estado(guardado.getEstado())
            .porcentaje(guardado.getPorcentajeCompletado())
            .estadoProfesor(guardado.getEstadoProfesor())
            .revisado(Boolean.TRUE.equals(guardado.getRevisado()))
            .build();
    }

    /**
     * Limpia el estado asignado por el profesor, dejando solo el estado calculado.
     */
    public ProgresoUsuarioDTO.ProgresoSeccionDTO limpiarEstadoProfesor(
            Integer estudianteId,
            String seccionCodigo) {
        return actualizarEstadoProfesor(estudianteId, seccionCodigo, null);
    }

    /**
     * Obtiene el progreso de todos los estudiantes asignados a un tutor.
     */
    @Transactional(readOnly = true)
    public List<EstudianteProgresoResumenDTO> obtenerProgresoEstudiantesPorTutor(Integer tutorId) {
        log.debug("Obteniendo progreso de estudiantes para tutor {}", tutorId);

        List<Usuario> estudiantes = tutorEstudianteRepository.findEstudiantesByTutorId(tutorId);

        return estudiantes.stream().map(estudiante -> {
            ProgresoUsuarioDTO progreso = obtenerProgresoCompleto(estudiante.getId());
            return EstudianteProgresoResumenDTO.builder()
                    .estudianteId(estudiante.getId())
                    .nombreEstudiante(estudiante.getNombre())
                    .correoEstudiante(estudiante.getCorreo())
                    .porcentajeTotal(progreso.getPorcentajeTotal())
                    .estadoGeneral(progreso.getEstadoGeneral())
                    .secciones(progreso.getSecciones())
                    .build();
        }).collect(Collectors.toList());
    }

    private int calcularPorcentaje(String estado) {
        return switch (estado) {
            case "completado" -> 100;
            case "en_desarrollo" -> 50;
            default -> 0;
        };
    }

    private String calcularEstadoDesdeProgreso(int porcentaje) {
        if (porcentaje <= 0) {
            return "sin_avances";
        } else if (porcentaje >= 100) {
            return "completado";
        } else {
            return "en_desarrollo";
        }
    }
}
