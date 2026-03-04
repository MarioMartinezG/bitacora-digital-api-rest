package com.diginexa.bitacora.services;

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
    private final AsignaturaRepository asignaturaRepository;
    private final AlertaSistemaRepository alertaSistemaRepository;
    private final TutorEstudianteRepository tutorEstudianteRepository;

    public EstadisticasGeneralesDTO obtenerEstadisticasGenerales() {
        long totalEstudiantes = usuarioRepository.countByRolNombre("estudiante");
        long totalTutores = usuarioRepository.countByRolNombre("tutor");
        long totalAsignaturas = asignaturaRepository.countActivas();
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
                .totalAsignaturas(totalAsignaturas)
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
            List<ProgresoSeccion> progresos = progresoSeccionRepository.findByUsuarioId(estudiante.getId());

            List<ReporteProgresoDTO.SeccionProgresoDTO> secciones = progresos.stream()
                    .map(p -> ReporteProgresoDTO.SeccionProgresoDTO.builder()
                            .seccionCodigo(p.getSeccionCodigo())
                            .estado(p.getEstado())
                            .porcentaje(p.getPorcentajeCompletado())
                            .build())
                    .toList();

            double promedio = progresos.isEmpty() ? 0 :
                    progresos.stream()
                            .mapToInt(p -> p.getPorcentajeCompletado() != null ? p.getPorcentajeCompletado() : 0)
                            .average()
                            .orElse(0);

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

    private List<MetricaProgresoDTO> calcularProgresosPorSeccion() {
        List<ProgresoSeccion> todosLosProgresos = progresoSeccionRepository.findAll();
        long totalEstudiantes = usuarioRepository.countByRolNombre("estudiante");

        Map<String, List<ProgresoSeccion>> porSeccion = todosLosProgresos.stream()
                .collect(Collectors.groupingBy(ProgresoSeccion::getSeccionCodigo));

        return porSeccion.entrySet().stream()
                .map(entry -> {
                    String codigo = entry.getKey();
                    List<ProgresoSeccion> progresos = entry.getValue();

                    long completados = progresos.stream()
                            .filter(p -> "completado".equals(p.getEstado()))
                            .count();
                    long enDesarrollo = progresos.stream()
                            .filter(p -> "en_desarrollo".equals(p.getEstado()))
                            .count();
                    long sinAvances = totalEstudiantes - completados - enDesarrollo;

                    double porcentaje = totalEstudiantes > 0 ?
                            (completados * 100.0) / totalEstudiantes : 0;

                    return MetricaProgresoDTO.builder()
                            .seccionCodigo(codigo)
                            .seccionNombre(codigo)
                            .totalEstudiantes(totalEstudiantes)
                            .completados(completados)
                            .enDesarrollo(enDesarrollo)
                            .sinAvances(sinAvances)
                            .porcentajeCompletado(porcentaje)
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
