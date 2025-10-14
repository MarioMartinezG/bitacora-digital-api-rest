package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.SeccionConCamposDTO;
import com.diginexa.bitacora.entities.CampoSeccion;
import com.diginexa.bitacora.entities.Seccion;
import com.diginexa.bitacora.exceptions.domain.ModuloNotFoundException;
import com.diginexa.bitacora.exceptions.domain.SeccionNotFoundException;
import com.diginexa.bitacora.repositories.ModuloRepository;
import com.diginexa.bitacora.repositories.SeccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeccionService {

    private final SeccionRepository seccionRepository;
    private final ModuloRepository moduloRepository;

    public List<Seccion> findByModuloId(Long moduloId) {
        log.info("Buscando secciones para el módulo: {}", moduloId);

        // Verificar que el módulo existe
        if (!moduloRepository.existsById(moduloId)) {
            throw new ModuloNotFoundException(moduloId);
        }

        return seccionRepository.findByModuloIdWithCampos(moduloId);
    }

    public Seccion findByIdWithCampos(Long seccionId) {
        log.info("Buscando sección con campos: {}", seccionId);
        return seccionRepository.findByIdWithCampos(seccionId)
                .orElseThrow(() -> new SeccionNotFoundException(seccionId));
    }

    public SeccionConCamposDTO findSeccionConCamposDTO(Long seccionId) {
        Seccion seccion = findByIdWithCampos(seccionId);
        return convertToDTO(seccion);
    }

    public List<SeccionConCamposDTO> findSeccionesByModuloId(Long moduloId) {
        List<Seccion> secciones = findByModuloId(moduloId);
        return secciones.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SeccionConCamposDTO convertToDTO(Seccion seccion) {
        // Manejar el caso cuando campos es null
        List<CampoSeccion> campos = seccion.getCampos();
        List<SeccionConCamposDTO.CampoSeccionDTO> camposDTO = (campos != null)
                ? campos.stream()
                .map(this::convertCampoToDTO)
                .collect(Collectors.toList())
                : Collections.emptyList(); // Lista vacía si es null

        return SeccionConCamposDTO.builder()
                .id(seccion.getId())
                .nombre(seccion.getNombre())
                .tipoSeccion(seccion.getTipoSeccion())
                .orden(seccion.getOrden())
                .configuracion(seccion.getConfiguracion())
                .campos(camposDTO)
                .build();
    }

    private SeccionConCamposDTO.CampoSeccionDTO convertCampoToDTO(CampoSeccion campo) {
        return SeccionConCamposDTO.CampoSeccionDTO.builder()
                .id(campo.getId())
                .label(campo.getLabel())
                .tipoCampo(campo.getTipoCampo())
                .opciones(campo.getOpciones())
                .esRequerido(campo.getEsRequerido())
                .orden(campo.getOrden())
                .configuracion(campo.getConfiguracion())
                .placeholder(campo.getPlaceholder())
                .build();
    }
}
