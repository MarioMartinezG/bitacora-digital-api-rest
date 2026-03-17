package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.CrearDimensionRequest;
import com.diginexa.bitacora.dtos.bitacora.DimensionDTO;
import com.diginexa.bitacora.entities.Dimension;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.repositories.DimensionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DimensionService {

    private final DimensionRepository dimensionRepository;

    @Transactional(readOnly = true)
    public List<DimensionDTO> listarActivas() {
        return dimensionRepository.findByActivoTrueOrderByNombreAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DimensionDTO> listarTodas() {
        return dimensionRepository.findAllByOrderByNombreAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public DimensionDTO crear(CrearDimensionRequest request) {
        String nombre = request.getNombre().trim();
        if (dimensionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una dimensión con el nombre: " + nombre);
        }
        Dimension dimension = Dimension.builder()
                .nombre(nombre)
                .build();
        Dimension guardada = dimensionRepository.save(dimension);
        log.info("Dimensión creada: id={}, nombre={}", guardada.getId(), guardada.getNombre());
        return toDTO(guardada);
    }

    public DimensionDTO actualizar(Long id, CrearDimensionRequest request) {
        Dimension dimension = dimensionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dimensión no encontrada con ID: " + id));
        String nombre = request.getNombre().trim();
        if (!dimension.getNombre().equalsIgnoreCase(nombre) && dimensionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una dimensión con el nombre: " + nombre);
        }
        dimension.setNombre(nombre);
        Dimension guardada = dimensionRepository.save(dimension);
        log.info("Dimensión actualizada: id={}", id);
        return toDTO(guardada);
    }

    public DimensionDTO toggleActivo(Long id) {
        Dimension dimension = dimensionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dimensión no encontrada con ID: " + id));
        dimension.setActivo(!Boolean.TRUE.equals(dimension.getActivo()));
        Dimension guardada = dimensionRepository.save(dimension);
        log.info("Dimensión id={} activo={}", id, guardada.getActivo());
        return toDTO(guardada);
    }

    private DimensionDTO toDTO(Dimension d) {
        return DimensionDTO.builder()
                .id(d.getId())
                .nombre(d.getNombre())
                .activo(d.getActivo())
                .build();
    }
}
