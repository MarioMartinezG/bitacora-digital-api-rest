package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.GuardarSeccionRequest;
import com.diginexa.bitacora.dtos.bitacora.RespuestaSeccionDTO;
import com.diginexa.bitacora.services.BitacoraSeccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bitacora/secciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BitacoraSeccionController {

    private final BitacoraSeccionService seccionService;

    /**
     * Obtener respuestas de una sección para un usuario
     * GET /api/bitacora/secciones/{seccionCodigo}/usuario/{usuarioId}
     */
    @GetMapping("/{seccionCodigo}/usuario/{usuarioId}")
    public ResponseEntity<RespuestaSeccionDTO> obtenerSeccion(
            @PathVariable String seccionCodigo,
            @PathVariable Integer usuarioId) {
        log.info("GET /api/bitacora/secciones/{}/usuario/{}", seccionCodigo, usuarioId);
        RespuestaSeccionDTO respuesta = seccionService.obtenerRespuesta(usuarioId, seccionCodigo);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Guardar/actualizar respuestas de una sección
     * PUT /api/bitacora/secciones/{seccionCodigo}
     */
    @PutMapping("/{seccionCodigo}")
    public ResponseEntity<RespuestaSeccionDTO> guardarSeccion(
            @PathVariable String seccionCodigo,
            @RequestBody @Valid GuardarSeccionRequest request) {
        log.info("PUT /api/bitacora/secciones/{} para usuario {}", seccionCodigo, request.getUsuarioId());

        // Asegurar que el código coincide
        request.setSeccionCodigo(seccionCodigo);

        RespuestaSeccionDTO respuesta = seccionService.guardarRespuesta(request);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtener todas las secciones de un usuario (para cargar estado inicial)
     * GET /api/bitacora/secciones/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, RespuestaSeccionDTO>> obtenerTodasSecciones(
            @PathVariable Integer usuarioId) {
        log.info("GET /api/bitacora/secciones/usuario/{}", usuarioId);
        Map<String, RespuestaSeccionDTO> respuestas = seccionService.obtenerTodasRespuestas(usuarioId);
        return ResponseEntity.ok(respuestas);
    }

    /**
     * Eliminar respuestas de una sección
     * DELETE /api/bitacora/secciones/{seccionCodigo}/usuario/{usuarioId}
     */
    @DeleteMapping("/{seccionCodigo}/usuario/{usuarioId}")
    public ResponseEntity<Void> eliminarSeccion(
            @PathVariable String seccionCodigo,
            @PathVariable Integer usuarioId) {
        log.info("DELETE /api/bitacora/secciones/{}/usuario/{}", seccionCodigo, usuarioId);
        seccionService.eliminarRespuesta(usuarioId, seccionCodigo);
        return ResponseEntity.noContent().build();
    }
}
