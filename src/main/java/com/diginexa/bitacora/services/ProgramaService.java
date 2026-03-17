package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.CrearProgramaRequest;
import com.diginexa.bitacora.dtos.bitacora.ProgramaDTO;
import com.diginexa.bitacora.entities.Programa;
import com.diginexa.bitacora.exceptions.domain.ResourceNotFoundException;
import com.diginexa.bitacora.repositories.ProgramaRepository;
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
public class ProgramaService {

    private final ProgramaRepository programaRepository;

    @Transactional(readOnly = true)
    public List<ProgramaDTO> obtenerProgramas() {
        return programaRepository.findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProgramaDTO> obtenerTodos() {
        return programaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProgramaDTO crearPrograma(CrearProgramaRequest request) {
        Programa programa = Programa.builder()
                .nombre(request.getNombre().trim())
                .build();
        Programa guardado = programaRepository.save(programa);
        log.info("Programa creado: id={}, nombre={}", guardado.getId(), guardado.getNombre());
        return toDTO(guardado);
    }

    public ProgramaDTO actualizarPrograma(Long id, CrearProgramaRequest request) {
        Programa programa = programaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programa no encontrado con ID: " + id));
        programa.setNombre(request.getNombre().trim());
        Programa guardado = programaRepository.save(programa);
        log.info("Programa actualizado: id={}", id);
        return toDTO(guardado);
    }

    public void eliminarPrograma(Long id) {
        if (!programaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Programa no encontrado con ID: " + id);
        }
        programaRepository.deleteById(id);
        log.info("Programa eliminado: id={}", id);
    }

    private ProgramaDTO toDTO(Programa p) {
        return ProgramaDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .activo(p.getActivo())
                .build();
    }
}
