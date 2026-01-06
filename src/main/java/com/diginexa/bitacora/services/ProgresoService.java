package com.diginexa.bitacora.services;

import com.diginexa.bitacora.constants.SeccionCodigos;
import com.diginexa.bitacora.dtos.bitacora.ProgresoUsuarioDTO;
import com.diginexa.bitacora.entities.ProgresoSeccion;
import com.diginexa.bitacora.repositories.ProgresoSeccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProgresoService {

    private final ProgresoSeccionRepository repository;

    @Transactional(readOnly = true)
    public ProgresoUsuarioDTO obtenerProgresoCompleto(Integer usuarioId) {
        log.debug("Obteniendo progreso completo para usuario {}", usuarioId);

        List<ProgresoSeccion> progresos = repository.findByUsuarioId(usuarioId);

        Map<String, ProgresoUsuarioDTO.ProgresoSeccionDTO> secciones = new HashMap<>();
        int completadas = 0;
        int totalSecciones = SeccionCodigos.TODOS.length;

        // Inicializar todas las secciones con estado sin_avances
        for (String codigo : SeccionCodigos.TODOS) {
            secciones.put(codigo, ProgresoUsuarioDTO.ProgresoSeccionDTO.builder()
                .seccionCodigo(codigo)
                .estado("sin_avances")
                .porcentaje(0)
                .build());
        }

        // Actualizar con datos reales
        for (ProgresoSeccion p : progresos) {
            ProgresoUsuarioDTO.ProgresoSeccionDTO dto = ProgresoUsuarioDTO.ProgresoSeccionDTO.builder()
                .seccionCodigo(p.getSeccionCodigo())
                .estado(p.getEstado())
                .porcentaje(p.getPorcentajeCompletado())
                .build();
            secciones.put(p.getSeccionCodigo(), dto);

            if ("completado".equals(p.getEstado())) {
                completadas++;
            }
        }

        int porcentajeTotal = (int) ((completadas * 100.0) / totalSecciones);
        String estadoGeneral = calcularEstadoGeneral(completadas, totalSecciones);

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
            String estado) {
        log.info("Actualizando estado de sección {} a {} para usuario {}",
                 seccionCodigo, estado, usuarioId);

        ProgresoSeccion progreso = repository
            .findByUsuarioIdAndSeccionCodigo(usuarioId, seccionCodigo)
            .orElse(ProgresoSeccion.builder()
                .usuarioId(usuarioId)
                .seccionCodigo(seccionCodigo)
                .build());

        progreso.setEstado(estado);
        progreso.setPorcentajeCompletado(calcularPorcentaje(estado));

        ProgresoSeccion guardado = repository.save(progreso);

        return ProgresoUsuarioDTO.ProgresoSeccionDTO.builder()
            .seccionCodigo(guardado.getSeccionCodigo())
            .estado(guardado.getEstado())
            .porcentaje(guardado.getPorcentajeCompletado())
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
                .build())
            .orElse(ProgresoUsuarioDTO.ProgresoSeccionDTO.builder()
                .seccionCodigo(seccionCodigo)
                .estado("sin_avances")
                .porcentaje(0)
                .build());
    }

    private int calcularPorcentaje(String estado) {
        return switch (estado) {
            case "completado" -> 100;
            case "en_desarrollo" -> 50;
            default -> 0;
        };
    }

    private String calcularEstadoGeneral(int completadas, int total) {
        if (completadas == 0) {
            return "sin_avances";
        } else if (completadas == total) {
            return "completado";
        } else {
            return "en_desarrollo";
        }
    }
}
