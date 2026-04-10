package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.CrearMedioRequest;
import com.diginexa.bitacora.dtos.bitacora.MedioDTO;
import com.diginexa.bitacora.entities.Medio;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.repositories.MedioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedioService {

    private final MedioRepository medioRepository;

    @Transactional(readOnly = true)
    public List<MedioDTO> obtenerActivos() {
        return medioRepository.findByActivoTrueOrderByCategoriaAscLabelAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedioDTO> obtenerTodos() {
        return medioRepository.findAllByOrderByCategoriaAscLabelAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MedioDTO crearMedio(CrearMedioRequest request) {
        String value = generarValueUnico(request.getLabel());
        Medio medio = Medio.builder()
                .label(request.getLabel().trim())
                .value(value)
                .categoria(request.getCategoria())
                .build();
        Medio guardado = medioRepository.save(medio);
        log.info("Medio creado: id={}, value={}, categoria={}", guardado.getId(), guardado.getValue(), guardado.getCategoria());
        return toDTO(guardado);
    }

    public MedioDTO actualizarMedio(Long id, CrearMedioRequest request) {
        Medio medio = medioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medio no encontrado con ID: " + id));
        medio.setLabel(request.getLabel().trim());
        medio.setCategoria(request.getCategoria());
        Medio guardado = medioRepository.save(medio);
        log.info("Medio actualizado: id={}", id);
        return toDTO(guardado);
    }

    public MedioDTO toggleActivo(Long id) {
        Medio medio = medioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medio no encontrado con ID: " + id));
        medio.setActivo(!Boolean.TRUE.equals(medio.getActivo()));
        Medio guardado = medioRepository.save(medio);
        log.info("Medio id={} activo={}", id, guardado.getActivo());
        return toDTO(guardado);
    }

    private String generarValueUnico(String label) {
        String base = Normalizer.normalize(label.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        String value = base;
        int suffix = 1;
        while (medioRepository.existsByValue(value)) {
            value = base + "_" + suffix++;
        }
        return value;
    }

    private MedioDTO toDTO(Medio m) {
        return MedioDTO.builder()
                .id(m.getId())
                .label(m.getLabel())
                .value(m.getValue())
                .categoria(m.getCategoria())
                .activo(m.getActivo())
                .build();
    }
}
