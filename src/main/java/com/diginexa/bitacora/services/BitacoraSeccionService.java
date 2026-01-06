package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.bitacora.GuardarSeccionRequest;
import com.diginexa.bitacora.dtos.bitacora.RespuestaSeccionDTO;
import com.diginexa.bitacora.entities.RespuestaSeccion;
import com.diginexa.bitacora.repositories.RespuestaSeccionRepository;
import com.diginexa.bitacora.validation.SeccionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BitacoraSeccionService {

    private final RespuestaSeccionRepository respuestaRepository;
    private final SeccionValidator validator;
    private final ProgresoService progresoService;

    @Transactional(readOnly = true)
    public RespuestaSeccionDTO obtenerRespuesta(Integer usuarioId, String seccionCodigo) {
        log.debug("Obteniendo respuesta para usuario {} y sección {}", usuarioId, seccionCodigo);

        Optional<RespuestaSeccion> respuesta = respuestaRepository
            .findByUsuarioIdAndSeccionCodigo(usuarioId, seccionCodigo);

        return respuesta.map(this::convertToDTO)
            .orElse(RespuestaSeccionDTO.builder()
                .usuarioId(usuarioId)
                .seccionCodigo(seccionCodigo)
                .datos(new HashMap<>())
                .estadoAvance("sin_avances")
                .build());
    }

    public RespuestaSeccionDTO guardarRespuesta(GuardarSeccionRequest request) {
        log.info("Guardando respuesta para usuario {} en sección {}",
                 request.getUsuarioId(), request.getSeccionCodigo());

        // Validar datos según tipo de sección (validación básica)
        List<String> errores = validator.validarSinExcepcion(
            request.getSeccionCodigo(),
            request.getDatos()
        );

        if (!errores.isEmpty()) {
            log.warn("Validación con advertencias: {}", errores);
            // No lanzamos excepción, solo logueamos - permitimos guardado parcial
        }

        // Buscar o crear respuesta
        RespuestaSeccion respuesta = respuestaRepository
            .findByUsuarioIdAndSeccionCodigo(request.getUsuarioId(), request.getSeccionCodigo())
            .orElse(RespuestaSeccion.builder()
                .usuarioId(request.getUsuarioId())
                .seccionCodigo(request.getSeccionCodigo())
                .build());

        // Actualizar datos
        respuesta.setDatos(request.getDatos());

        // Calcular estado si no se proporciona
        String estado = request.getEstadoAvance();
        if (estado == null || estado.isEmpty()) {
            estado = calcularEstadoAvance(request.getSeccionCodigo(), request.getDatos());
        }
        respuesta.setEstadoAvance(estado);

        // Guardar
        RespuestaSeccion guardada = respuestaRepository.save(respuesta);
        log.info("Respuesta guardada con ID: {}", guardada.getId());

        // Actualizar progreso
        progresoService.actualizarEstadoSeccion(
            request.getUsuarioId(),
            request.getSeccionCodigo(),
            estado
        );

        return convertToDTO(guardada);
    }

    @Transactional(readOnly = true)
    public Map<String, RespuestaSeccionDTO> obtenerTodasRespuestas(Integer usuarioId) {
        log.debug("Obteniendo todas las respuestas para usuario {}", usuarioId);

        List<RespuestaSeccion> respuestas = respuestaRepository.findByUsuarioId(usuarioId);

        Map<String, RespuestaSeccionDTO> resultado = new HashMap<>();
        for (RespuestaSeccion r : respuestas) {
            resultado.put(r.getSeccionCodigo(), convertToDTO(r));
        }

        return resultado;
    }

    public void eliminarRespuesta(Integer usuarioId, String seccionCodigo) {
        log.info("Eliminando respuesta para usuario {} en sección {}", usuarioId, seccionCodigo);
        respuestaRepository.deleteByUsuarioIdAndSeccionCodigo(usuarioId, seccionCodigo);
    }

    private String calcularEstadoAvance(String seccionCodigo, Map<String, Object> datos) {
        if (datos == null || datos.isEmpty()) {
            return "sin_avances";
        }

        long camposLlenos = datos.values().stream()
            .filter(v -> v != null && !v.toString().trim().isEmpty())
            .count();

        if (camposLlenos == 0) {
            return "sin_avances";
        } else if (camposLlenos < datos.size()) {
            return "en_desarrollo";
        } else {
            return "completado";
        }
    }

    private RespuestaSeccionDTO convertToDTO(RespuestaSeccion entidad) {
        return RespuestaSeccionDTO.builder()
            .id(entidad.getId())
            .usuarioId(entidad.getUsuarioId())
            .seccionCodigo(entidad.getSeccionCodigo())
            .datos(entidad.getDatos())
            .estadoAvance(entidad.getEstadoAvance())
            .fechaActualizacion(entidad.getFechaActualizacion())
            .build();
    }
}
