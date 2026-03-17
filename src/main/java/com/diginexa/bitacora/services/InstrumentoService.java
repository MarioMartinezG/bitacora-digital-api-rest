package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.CrearInstrumentoRequest;
import com.diginexa.bitacora.dtos.bitacora.InstrumentoDTO;
import com.diginexa.bitacora.entities.Instrumento;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.repositories.InstrumentoRepository;
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
public class InstrumentoService {

    private final InstrumentoRepository instrumentoRepository;

    @Transactional(readOnly = true)
    public List<InstrumentoDTO> listarActivos() {
        return instrumentoRepository.findByActivoTrueOrderByLabelAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InstrumentoDTO> listarTodos() {
        return instrumentoRepository.findAllByOrderByLabelAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public InstrumentoDTO crear(CrearInstrumentoRequest request) {
        String value = generarValueUnico(request.getLabel());
        Instrumento instrumento = Instrumento.builder()
                .label(request.getLabel().trim())
                .value(value)
                .build();
        Instrumento guardado = instrumentoRepository.save(instrumento);
        log.info("Instrumento creado: id={}, value={}", guardado.getId(), guardado.getValue());
        return toDTO(guardado);
    }

    public InstrumentoDTO actualizar(Long id, CrearInstrumentoRequest request) {
        Instrumento instrumento = instrumentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instrumento no encontrado con ID: " + id));
        instrumento.setLabel(request.getLabel().trim());
        Instrumento guardado = instrumentoRepository.save(instrumento);
        log.info("Instrumento actualizado: id={}", id);
        return toDTO(guardado);
    }

    public InstrumentoDTO toggleActivo(Long id) {
        Instrumento instrumento = instrumentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instrumento no encontrado con ID: " + id));
        instrumento.setActivo(!Boolean.TRUE.equals(instrumento.getActivo()));
        Instrumento guardado = instrumentoRepository.save(instrumento);
        log.info("Instrumento id={} activo={}", id, guardado.getActivo());
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
        while (instrumentoRepository.existsByValue(value)) {
            value = base + "_" + suffix++;
        }
        return value;
    }

    private InstrumentoDTO toDTO(Instrumento i) {
        return InstrumentoDTO.builder()
                .id(i.getId())
                .label(i.getLabel())
                .value(i.getValue())
                .activo(i.getActivo())
                .build();
    }
}
