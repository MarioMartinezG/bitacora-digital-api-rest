package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.TemaContenidoDTO;
import com.diginexa.bitacora.entities.TemaContenido;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.repositories.TemaContenidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TemaContenidoService {

    private final TemaContenidoRepository repository;

    @Transactional(readOnly = true)
    public List<TemaContenidoDTO> listarPorUsuario(Integer usuarioId) {
        log.debug("Listando temas para usuario {}", usuarioId);
        return repository.findByUsuarioIdOrderByNumeroTemaAsc(usuarioId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public TemaContenidoDTO crear(Integer usuarioId, TemaContenidoDTO dto) {
        log.info("Creando tema {} para usuario {}", dto.getNumeroTema(), usuarioId);

        // Verificar si ya existe un tema con ese número
        repository.findByUsuarioIdAndNumeroTema(usuarioId, dto.getNumeroTema())
            .ifPresent(t -> {
                throw new IllegalArgumentException(
                    "Ya existe un tema con número " + dto.getNumeroTema() + " para este usuario"
                );
            });

        TemaContenido entidad = TemaContenido.builder()
            .usuarioId(usuarioId)
            .numeroTema(dto.getNumeroTema())
            .nombreTema(dto.getNombreTema())
            .subtemas(dto.getSubtemas() != null ? dto.getSubtemas() : new ArrayList<>())
            .build();

        TemaContenido guardado = repository.save(entidad);
        log.info("Tema creado con ID: {}", guardado.getId());

        return convertToDTO(guardado);
    }

    public TemaContenidoDTO actualizar(Long id, TemaContenidoDTO dto) {
        log.info("Actualizando tema {}", id);

        TemaContenido entidad = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado: " + id));

        entidad.setNombreTema(dto.getNombreTema());
        if (dto.getSubtemas() != null) {
            entidad.setSubtemas(dto.getSubtemas());
        }

        TemaContenido guardado = repository.save(entidad);
        return convertToDTO(guardado);
    }

    public void eliminar(Long id) {
        log.info("Eliminando tema {}", id);

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tema no encontrado: " + id);
        }

        repository.deleteById(id);
    }

    public TemaContenidoDTO agregarSubtema(Long temaId, String subtema) {
        log.info("Agregando subtema al tema {}", temaId);

        TemaContenido entidad = repository.findById(temaId)
            .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado: " + temaId));

        if (entidad.getSubtemas() == null) {
            entidad.setSubtemas(new ArrayList<>());
        }

        entidad.getSubtemas().add(subtema.trim());
        TemaContenido guardado = repository.save(entidad);

        return convertToDTO(guardado);
    }

    public TemaContenidoDTO eliminarSubtema(Long temaId, Integer indice) {
        log.info("Eliminando subtema {} del tema {}", indice, temaId);

        TemaContenido entidad = repository.findById(temaId)
            .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado: " + temaId));

        if (entidad.getSubtemas() == null || indice < 0 || indice >= entidad.getSubtemas().size()) {
            throw new IllegalArgumentException("Índice de subtema inválido: " + indice);
        }

        entidad.getSubtemas().remove(indice.intValue());
        TemaContenido guardado = repository.save(entidad);

        return convertToDTO(guardado);
    }

    public TemaContenidoDTO actualizarSubtema(Long temaId, Integer indice, String nuevoSubtema) {
        log.info("Actualizando subtema {} del tema {}", indice, temaId);

        TemaContenido entidad = repository.findById(temaId)
            .orElseThrow(() -> new ResourceNotFoundException("Tema no encontrado: " + temaId));

        if (entidad.getSubtemas() == null || indice < 0 || indice >= entidad.getSubtemas().size()) {
            throw new IllegalArgumentException("Índice de subtema inválido: " + indice);
        }

        entidad.getSubtemas().set(indice, nuevoSubtema.trim());
        TemaContenido guardado = repository.save(entidad);

        return convertToDTO(guardado);
    }

    private TemaContenidoDTO convertToDTO(TemaContenido entidad) {
        return TemaContenidoDTO.builder()
            .id(entidad.getId())
            .numeroTema(entidad.getNumeroTema())
            .nombreTema(entidad.getNombreTema())
            .subtemas(entidad.getSubtemas())
            .build();
    }
}
