package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.CrearMetodologiaRequest;
import com.diginexa.bitacora.dtos.bitacora.MetodologiaDTO;
import com.diginexa.bitacora.entities.Metodologia;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.repositories.MetodologiaRepository;
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
public class MetodologiaService {

    private final MetodologiaRepository metodologiaRepository;

    @Transactional(readOnly = true)
    public List<MetodologiaDTO> listarActivas() {
        return metodologiaRepository.findByActivoTrueOrderByLabelAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MetodologiaDTO> listarTodas() {
        return metodologiaRepository.findAllByOrderByLabelAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public MetodologiaDTO crear(CrearMetodologiaRequest request) {
        String value = generarValueUnico(request.getLabel());
        Metodologia metodologia = Metodologia.builder()
                .label(request.getLabel().trim())
                .value(value)
                .build();
        Metodologia guardada = metodologiaRepository.save(metodologia);
        log.info("Metodología creada: id={}, value={}", guardada.getId(), guardada.getValue());
        return toDTO(guardada);
    }

    public MetodologiaDTO actualizar(Long id, CrearMetodologiaRequest request) {
        Metodologia metodologia = metodologiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metodología no encontrada con ID: " + id));
        // value is immutable on edit — only label is updated
        metodologia.setLabel(request.getLabel().trim());
        Metodologia guardada = metodologiaRepository.save(metodologia);
        log.info("Metodología actualizada: id={}", id);
        return toDTO(guardada);
    }

    public MetodologiaDTO toggleActivo(Long id) {
        Metodologia metodologia = metodologiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metodología no encontrada con ID: " + id));
        metodologia.setActivo(!Boolean.TRUE.equals(metodologia.getActivo()));
        Metodologia guardada = metodologiaRepository.save(metodologia);
        log.info("Metodología id={} activo={}", id, guardada.getActivo());
        return toDTO(guardada);
    }

    private String generarValueUnico(String label) {
        String base = Normalizer.normalize(label.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        String value = base;
        int suffix = 1;
        while (metodologiaRepository.existsByValue(value)) {
            value = base + "_" + suffix++;
        }
        return value;
    }

    private MetodologiaDTO toDTO(Metodologia m) {
        return MetodologiaDTO.builder()
                .id(m.getId())
                .label(m.getLabel())
                .value(m.getValue())
                .activo(m.getActivo())
                .build();
    }
}
