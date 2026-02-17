package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.CrearMomentoRequest;
import com.diginexa.bitacora.dtos.bitacora.MomentoDTO;
import com.diginexa.bitacora.entities.Momento;
import com.diginexa.bitacora.entities.MomentoSeccion;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.repositories.MomentoRepository;
import com.diginexa.bitacora.repositories.MomentoSeccionRepository;
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
public class MomentoService {

    private final MomentoRepository momentoRepository;
    private final MomentoSeccionRepository momentoSeccionRepository;

    public MomentoDTO crearMomento(CrearMomentoRequest request) {
        Momento momento = Momento.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .fechaLimite(request.getFechaLimite())
                .build();

        Momento guardado = momentoRepository.save(momento);
        log.info("Momento creado: id={}, nombre={}", guardado.getId(), guardado.getNombre());

        if (request.getSecciones() != null && !request.getSecciones().isEmpty()) {
            asociarSeccionesInterno(guardado.getId(), request.getSecciones());
        }

        return convertToDTO(guardado);
    }

    public MomentoDTO actualizarMomento(Long id, CrearMomentoRequest request) {
        Momento momento = momentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Momento no encontrado con ID: " + id));

        momento.setNombre(request.getNombre());
        momento.setDescripcion(request.getDescripcion());
        momento.setFechaLimite(request.getFechaLimite());

        Momento guardado = momentoRepository.save(momento);

        // Reemplazar secciones asociadas
        momentoSeccionRepository.deleteByMomentoId(id);
        if (request.getSecciones() != null && !request.getSecciones().isEmpty()) {
            asociarSeccionesInterno(id, request.getSecciones());
        }

        log.info("Momento actualizado: id={}", id);
        return convertToDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<MomentoDTO> obtenerMomentos() {
        return momentoRepository.findByActivoTrueOrderByFechaLimiteAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MomentoDTO obtenerMomento(Long id) {
        Momento momento = momentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Momento no encontrado con ID: " + id));
        return convertToDTO(momento);
    }

    public void eliminarMomento(Long id) {
        if (!momentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Momento no encontrado con ID: " + id);
        }
        momentoRepository.deleteById(id);
        log.info("Momento eliminado: id={}", id);
    }

    private void asociarSeccionesInterno(Long momentoId, List<String> secciones) {
        List<MomentoSeccion> asociaciones = secciones.stream()
                .map(s -> MomentoSeccion.builder()
                        .momentoId(momentoId)
                        .seccionCodigo(s)
                        .build())
                .collect(Collectors.toList());
        momentoSeccionRepository.saveAll(asociaciones);
    }

    private MomentoDTO convertToDTO(Momento entity) {
        List<String> secciones = momentoSeccionRepository.findByMomentoId(entity.getId())
                .stream()
                .map(MomentoSeccion::getSeccionCodigo)
                .collect(Collectors.toList());

        return MomentoDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .fechaLimite(entity.getFechaLimite())
                .activo(entity.getActivo())
                .secciones(secciones)
                .build();
    }
}
