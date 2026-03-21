package com.diginexa.bitacora.services;

import com.diginexa.bitacora.constants.SeccionCodigos;
import com.diginexa.bitacora.dtos.coordinador.*;
import com.diginexa.bitacora.entities.AlertaSistema;
import com.diginexa.bitacora.entities.ProgresoSeccion;
import com.diginexa.bitacora.entities.Usuario;
import com.diginexa.bitacora.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EstadisticasCoordinadorService {

    private final UsuarioRepository usuarioRepository;
    private final ProgresoSeccionRepository progresoSeccionRepository;
    private final AlertaSistemaRepository alertaSistemaRepository;
    private final TutorEstudianteRepository tutorEstudianteRepository;

    public EstadisticasGeneralesDTO obtenerEstadisticasGenerales() {
        long totalEstudiantes = usuarioRepository.countByRolNombre("estudiante");
        long totalTutores = usuarioRepository.countByRolNombre("tutor");
        long estudiantesActivos = usuarioRepository.countActivos();
        long alertasPendientes = alertaSistemaRepository.countByResueltaFalse();

        List<MetricaProgresoDTO> progresosPorSeccion = calcularProgresosPorSeccion();

        double promedioProgreso = progresosPorSeccion.isEmpty() ? 0 :
                progresosPorSeccion.stream()
                        .mapToDouble(MetricaProgresoDTO::getPorcentajeCompletado)
                        .average()
                        .orElse(0);

        return EstadisticasGeneralesDTO.builder()
                .totalEstudiantes(totalEstudiantes)
                .totalTutores(totalTutores)
                .estudiantesActivos(estudiantesActivos)
                .promedioProgreso(promedioProgreso)
                .alertasPendientes(alertasPendientes)
                .progresosPorSeccion(progresosPorSeccion)
                .build();
    }

    public List<AlertaSistemaDTO> obtenerAlertasPendientes() {
        return alertaSistemaRepository.findByResueltaFalseOrderByFechaCreacionDesc().stream()
                .map(this::toAlertaDTO)
                .toList();
    }

    @Transactional
    public void resolverAlerta(Integer alertaId) {
        AlertaSistema alerta = alertaSistemaRepository.findById(alertaId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Alerta no encontrada"));
        alerta.setResuelta(true);
        alerta.setFechaResolucion(java.time.LocalDateTime.now());
        alertaSistemaRepository.save(alerta);
    }

    public List<ReporteProgresoDTO> obtenerReportesProgreso() {
        List<Usuario> estudiantes = usuarioRepository.findByRolNombre("estudiante");

        return estudiantes.stream().map(estudiante -> {
            // Solo módulos activos (excluye ajustes, rap-rac u otros obsoletos)
            List<ProgresoSeccion> progresos = progresoSeccionRepository.findByUsuarioId(estudiante.getId())
                    .stream()
                    .filter(p -> SeccionCodigos.esValido(p.getSeccionCodigo()))
                    .toList();

            List<ReporteProgresoDTO.SeccionProgresoDTO> secciones = progresos.stream()
                    .map(p -> ReporteProgresoDTO.SeccionProgresoDTO.builder()
                            .seccionCodigo(p.getSeccionCodigo())
                            .estado(p.getEstado())
                            .porcentaje(p.getPorcentajeCompletado())
                            .build())
                    .toList();

            // Dividir entre el total de módulos activos (no solo los que tienen registro en BD)
            // para que coincida con el cálculo de ProgresoService
            int totalModulos = SeccionCodigos.TODOS.length;
            int sumaPorcentajes = progresos.stream()
                    .mapToInt(p -> p.getPorcentajeCompletado() != null ? p.getPorcentajeCompletado() : 0)
                    .sum();
            double promedio = totalModulos > 0 ? (double) sumaPorcentajes / totalModulos : 0;

            String tutorNombre = tutorEstudianteRepository.findTutorByEstudianteId(estudiante.getId())
                    .map(Usuario::getNombre)
                    .orElse("Sin tutor asignado");

            return ReporteProgresoDTO.builder()
                    .estudianteId(estudiante.getId())
                    .estudianteNombre(estudiante.getNombre())
                    .estudianteCorreo(estudiante.getCorreo())
                    .tutorNombre(tutorNombre)
                    .porcentajeGeneral(promedio)
                    .secciones(secciones)
                    .build();
        }).toList();
    }

    private static final Map<String, String> NOMBRES_SECCIONES = Map.of(
        SeccionCodigos.OBSERVAR,     "Observar, registrar y actuar de manera oportuna",
        SeccionCodigos.CARACTERIZA,  "Identificación de tu curso",
        SeccionCodigos.FACTORES,     "Factores Situacionales",
        SeccionCodigos.ACTIVIDADES,  "Actividades de Aprendizaje",
        SeccionCodigos.EVALUACION,   "Diseño de la evaluación",
        SeccionCodigos.SECUENCIA,    "Secuencia y cronograma",
        SeccionCodigos.CALIFICACION, "Calificación",
        SeccionCodigos.BIBLIOGRAFIA, "Medios educativos"
    );

    private List<MetricaProgresoDTO> calcularProgresosPorSeccion() {
        List<ProgresoSeccion> todosLosProgresos = progresoSeccionRepository.findAll();
        long totalEstudiantes = usuarioRepository.countByRolNombre("estudiante");

        // Solo módulos activos; agrupa los registros existentes por sección
        Map<String, List<ProgresoSeccion>> porSeccion = todosLosProgresos.stream()
                .filter(p -> SeccionCodigos.esValido(p.getSeccionCodigo()))
                .collect(Collectors.groupingBy(ProgresoSeccion::getSeccionCodigo));

        // Itera sobre TODOS para garantizar los 8 módulos aunque no haya registros aún
        return Arrays.stream(SeccionCodigos.TODOS)
                .map(codigo -> {
                    List<ProgresoSeccion> progresos = porSeccion.getOrDefault(codigo, List.of());

                    long completados = progresos.stream()
                            .filter(p -> "completado".equals(p.getEstado()))
                            .count();
                    long enDesarrollo = progresos.stream()
                            .filter(p -> "en_desarrollo".equals(p.getEstado()))
                            .count();
                    long sinAvances = totalEstudiantes - completados - enDesarrollo;

                    double porcentaje = totalEstudiantes > 0 ?
                            (completados * 100.0) / totalEstudiantes : 0;

                    double promedioProgreso = progresos.isEmpty() ? 0 :
                            progresos.stream()
                                    .mapToInt(p -> p.getPorcentajeCompletado() != null ? p.getPorcentajeCompletado() : 0)
                                    .average()
                                    .orElse(0);

                    return MetricaProgresoDTO.builder()
                            .seccionCodigo(codigo)
                            .seccionNombre(NOMBRES_SECCIONES.getOrDefault(codigo, codigo))
                            .totalEstudiantes(totalEstudiantes)
                            .completados(completados)
                            .enDesarrollo(enDesarrollo)
                            .sinAvances(sinAvances)
                            .porcentajeCompletado(porcentaje)
                            .promedioProgreso(promedioProgreso)
                            .build();
                })
                .toList();
    }

    private AlertaSistemaDTO toAlertaDTO(AlertaSistema alerta) {
        return AlertaSistemaDTO.builder()
                .id(alerta.getId())
                .tipo(alerta.getTipo())
                .prioridad(alerta.getPrioridad())
                .titulo(alerta.getTitulo())
                .mensaje(alerta.getMensaje())
                .usuarioReferenciaId(alerta.getUsuarioReferenciaId())
                .usuarioReferenciaNombre(
                        alerta.getUsuarioReferencia() != null ? alerta.getUsuarioReferencia().getNombre() : null
                )
                .datosAdicionales(alerta.getDatosAdicionales())
                .resuelta(alerta.getResuelta())
                .fechaCreacion(alerta.getFechaCreacion())
                .build();
    }
}
