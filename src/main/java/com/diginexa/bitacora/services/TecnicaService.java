package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.CrearTecnicaRequest;
import com.diginexa.bitacora.dtos.bitacora.TecnicaDTO;
import com.diginexa.bitacora.entities.Tecnica;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.repositories.TecnicaRepository;
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
public class TecnicaService {

    private final TecnicaRepository tecnicaRepository;

    @Transactional(readOnly = true)
    public List<TecnicaDTO> listarActivas() {
        return tecnicaRepository.findByActivoTrueOrderByGrupoAscLabelAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TecnicaDTO> listarTodas() {
        return tecnicaRepository.findAllByOrderByGrupoAscLabelAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TecnicaDTO crear(CrearTecnicaRequest request) {
        String value = generarValueUnico(request.getLabel());
        Tecnica tecnica = Tecnica.builder()
                .label(request.getLabel().trim())
                .value(value)
                .grupo(request.getGrupo())
                .build();
        Tecnica guardada = tecnicaRepository.save(tecnica);
        log.info("Técnica creada: id={}, value={}, grupo={}", guardada.getId(), guardada.getValue(), guardada.getGrupo());
        return toDTO(guardada);
    }

    public TecnicaDTO actualizar(Long id, CrearTecnicaRequest request) {
        Tecnica tecnica = tecnicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnica no encontrada con ID: " + id));
        tecnica.setLabel(request.getLabel().trim());
        tecnica.setGrupo(request.getGrupo());
        Tecnica guardada = tecnicaRepository.save(tecnica);
        log.info("Técnica actualizada: id={}", id);
        return toDTO(guardada);
    }

    public TecnicaDTO toggleActivo(Long id) {
        Tecnica tecnica = tecnicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Técnica no encontrada con ID: " + id));
        tecnica.setActivo(!Boolean.TRUE.equals(tecnica.getActivo()));
        Tecnica guardada = tecnicaRepository.save(tecnica);
        log.info("Técnica id={} activo={}", id, guardada.getActivo());
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
        while (tecnicaRepository.existsByValue(value)) {
            value = base + "_" + suffix++;
        }
        return value;
    }

    private TecnicaDTO toDTO(Tecnica t) {
        return TecnicaDTO.builder()
                .id(t.getId())
                .label(t.getLabel())
                .value(t.getValue())
                .grupo(t.getGrupo())
                .activo(t.getActivo())
                .build();
    }
}
