package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.RespuestaRequest;
import com.diginexa.bitacora.entities.CampoSeccion;
import com.diginexa.bitacora.entities.Respuesta;
import com.diginexa.bitacora.entities.Seccion;
import com.diginexa.bitacora.exceptions.domain.CampoNotFoundException;
import com.diginexa.bitacora.exceptions.domain.SeccionNotFoundException;
import com.diginexa.bitacora.exceptions.validation.RespuestaValidationException;
import com.diginexa.bitacora.repositories.CampoSeccionRepository;
import com.diginexa.bitacora.repositories.RespuestaRepository;
import com.diginexa.bitacora.repositories.SeccionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RespuestaService {

    private final RespuestaRepository respuestaRepository;
    private final SeccionRepository seccionRepository;
    private final CampoSeccionRepository campoSeccionRepository;

    public List<Respuesta> guardarEnLote(List<RespuestaRequest> requests) {
        log.info("Guardando {} respuestas en lote", requests.size());

        // Validar que la lista no esté vacía
        if (requests.isEmpty()) {
            throw new RespuestaValidationException("La lista de respuestas no puede estar vacía");
        }

        List<Respuesta> respuestasGuardadas = new ArrayList<>();

        for (RespuestaRequest request : requests) {
            Respuesta respuesta = guardarRespuestaIndividual(request);
            respuestasGuardadas.add(respuesta);
        }

        log.info("Respuestas guardadas exitosamente: {}", respuestasGuardadas.size());
        return respuestasGuardadas;
    }

    public Respuesta guardarRespuestaIndividual(RespuestaRequest request) {
        log.debug("Guardando respuesta para usuario: {}, campo: {}",
                request.getUsuarioId(), request.getCampoId());

        // Validaciones
        if (request.getUsuarioId() == null) {
            throw new RespuestaValidationException("El ID de usuario es requerido");
        }

        if (request.getCampoId() == null) {
            throw new RespuestaValidationException("El ID de campo es requerido");
        }

        // Buscar respuesta existente
        Optional<Respuesta> respuestaExistente = respuestaRepository
                .findByUsuarioIdAndCampoId(request.getUsuarioId(), request.getCampoId());

        Respuesta respuesta;

        if (respuestaExistente.isPresent()) {
            // Actualizar respuesta existente
            respuesta = respuestaExistente.get();
            respuesta.setRespuestaTexto(request.getRespuestaTexto());
            respuesta.setRespuestaJson(convertToMap(request.getRespuestaJson()));
            respuesta.setEstadoAvance(request.getEstadoAvance());
        } else {
            // Crear nueva respuesta
            Seccion seccion = seccionRepository.findById(request.getSeccionId())
                    .orElseThrow(() -> new SeccionNotFoundException(request.getSeccionId()));

            CampoSeccion campo = campoSeccionRepository.findById(request.getCampoId())
                    .orElseThrow(() -> new CampoNotFoundException(request.getCampoId()));

            respuesta = Respuesta.builder()
                    .usuarioId(request.getUsuarioId())
                    .seccion(seccion)
                    .campo(campo)
                    .respuestaTexto(request.getRespuestaTexto())
                    .respuestaJson(convertToMap(request.getRespuestaJson()))
                    .estadoAvance(request.getEstadoAvance())
                    .build();
        }

        return respuestaRepository.save(respuesta);
    }

    public List<Respuesta> findByUsuarioIdAndModuloId(Integer usuarioId, Long moduloId) {
        log.info("Buscando respuestas para usuario: {}, módulo: {}", usuarioId, moduloId);

        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID de usuario no puede ser nulo");
        }

        if (moduloId == null) {
            throw new IllegalArgumentException("El ID de módulo no puede ser nulo");
        }

        return respuestaRepository.findByUsuarioIdAndModuloId(usuarioId, moduloId);
    }

    public List<Respuesta> findByUsuarioIdAndSeccionId(Integer usuarioId, Long seccionId) {
        log.info("Buscando respuestas para usuario: {}, sección: {}", usuarioId, seccionId);

        if (usuarioId == null || seccionId == null) {
            throw new IllegalArgumentException("Usuario ID y Sección ID son requeridos");
        }

        return respuestaRepository.findByUsuarioIdAndSeccionId(usuarioId, seccionId);
    }

    public void eliminarRespuesta(Integer usuarioId, Long campoId) {
        log.info("Eliminando respuesta para usuario: {}, campo: {}", usuarioId, campoId);

        if (usuarioId == null || campoId == null) {
            throw new IllegalArgumentException("Usuario ID y Campo ID son requeridos");
        }

        if (!respuestaRepository.existsByUsuarioIdAndCampoId(usuarioId, campoId)) {
            throw new RuntimeException("Respuesta no encontrada para el usuario y campo especificados");
        }

        respuestaRepository.deleteByUsuarioIdAndCampoId(usuarioId, campoId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object jsonObject) {
        switch (jsonObject) {
            case null -> {
                return null;
            }
            case Map map -> {
                return (Map<String, Object>) jsonObject;
            }


            // Si es String, intentar parsear como JSON
            case String s -> {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    return objectMapper.readValue((String) jsonObject, Map.class);
                } catch (Exception e) {
                    log.warn("No se pudo parsear el JSON, guardando como texto simple");
                    return Map.of("valor", jsonObject);
                }
            }
            default -> {
            }
        }

        // Para otros tipos, crear un mapa simple
        return Map.of("valor", jsonObject);
    }
}
