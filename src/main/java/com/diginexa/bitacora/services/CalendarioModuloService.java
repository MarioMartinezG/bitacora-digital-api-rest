package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.notificacion.CalendarioModuloDTO;
import com.diginexa.bitacora.entities.CalendarioModulo;
import com.diginexa.bitacora.exceptions.domain.CalendarioModuloNotFoundException;
import com.diginexa.bitacora.repositories.CalendarioModuloRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CalendarioModuloService {

    private final CalendarioModuloRepository repository;

    @Transactional(readOnly = true)
    public List<CalendarioModuloDTO> listarTodos() {
        return repository.findByActivoTrueOrderByFechaLimiteAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CalendarioModuloDTO obtenerPorSeccion(String seccionCodigo) {
        return repository.findBySeccionCodigo(seccionCodigo)
                .map(this::convertToDTO)
                .orElseThrow(() -> new CalendarioModuloNotFoundException(seccionCodigo));
    }

    @Transactional(readOnly = true)
    public CalendarioModuloDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new CalendarioModuloNotFoundException(id));
    }

    public CalendarioModuloDTO crear(CalendarioModuloDTO dto) {
        log.info("Creando calendario para sección: {}", dto.getSeccionCodigo());

        if (repository.existsBySeccionCodigo(dto.getSeccionCodigo())) {
            throw new IllegalArgumentException("Ya existe un calendario para la sección: " + dto.getSeccionCodigo());
        }

        CalendarioModulo entidad = CalendarioModulo.builder()
                .seccionCodigo(dto.getSeccionCodigo())
                .nombreModulo(dto.getNombreModulo())
                .fechaLimite(dto.getFechaLimite())
                .descripcion(dto.getDescripcion())
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        CalendarioModulo guardado = repository.save(entidad);
        log.info("Calendario creado con ID: {}", guardado.getId());

        return convertToDTO(guardado);
    }

    public CalendarioModuloDTO actualizar(Long id, CalendarioModuloDTO dto) {
        log.info("Actualizando calendario ID: {}", id);

        CalendarioModulo entidad = repository.findById(id)
                .orElseThrow(() -> new CalendarioModuloNotFoundException(id));

        if (dto.getNombreModulo() != null) {
            entidad.setNombreModulo(dto.getNombreModulo());
        }
        if (dto.getFechaLimite() != null) {
            entidad.setFechaLimite(dto.getFechaLimite());
        }
        if (dto.getDescripcion() != null) {
            entidad.setDescripcion(dto.getDescripcion());
        }
        if (dto.getActivo() != null) {
            entidad.setActivo(dto.getActivo());
        }

        CalendarioModulo guardado = repository.save(entidad);
        log.info("Calendario actualizado: {}", guardado.getId());

        return convertToDTO(guardado);
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new CalendarioModuloNotFoundException(id);
        }
        repository.deleteById(id);
        log.info("Calendario eliminado: {}", id);
    }

    @Transactional(readOnly = true)
    public List<CalendarioModuloDTO> obtenerProximosVencimientos(int diasAdelante) {
        LocalDate hoy = LocalDate.now();
        LocalDate hasta = hoy.plusDays(diasAdelante);

        return repository.findByFechaLimiteBetween(hoy, hasta)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CalendarioModuloDTO convertToDTO(CalendarioModulo entidad) {
        LocalDate hoy = LocalDate.now();
        int diasRestantes = (int) ChronoUnit.DAYS.between(hoy, entidad.getFechaLimite());

        String estadoVencimiento;
        if (diasRestantes < 0) {
            estadoVencimiento = "VENCIDO";
        } else if (diasRestantes <= 3) {
            estadoVencimiento = "PROXIMO";
        } else {
            estadoVencimiento = "NORMAL";
        }

        return CalendarioModuloDTO.builder()
                .id(entidad.getId())
                .seccionCodigo(entidad.getSeccionCodigo())
                .nombreModulo(entidad.getNombreModulo())
                .fechaLimite(entidad.getFechaLimite())
                .descripcion(entidad.getDescripcion())
                .activo(entidad.getActivo())
                .fechaCreacion(entidad.getFechaCreacion())
                .fechaActualizacion(entidad.getFechaActualizacion())
                .diasRestantes(diasRestantes)
                .estadoVencimiento(estadoVencimiento)
                .build();
    }
}
