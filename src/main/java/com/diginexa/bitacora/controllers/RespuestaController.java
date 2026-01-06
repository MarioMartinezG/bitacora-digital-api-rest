package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.RespuestaRequest;
import com.diginexa.bitacora.entities.Respuesta;
import com.diginexa.bitacora.services.RespuestaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @deprecated Desde versión 2.0. Usar BitacoraSeccionController para guardar respuestas.
 * @see com.diginexa.bitacora.controllers.BitacoraSeccionController
 */
@Deprecated(since = "2.0", forRemoval = true)
@RestController
@RequestMapping("/api/respuestas")
@RequiredArgsConstructor
@Slf4j
public class RespuestaController {

    private final RespuestaService respuestaService;

    @PostMapping("/lote")
    public ResponseEntity<List<Respuesta>> guardarRespuestasEnLote(@RequestBody @Valid List<RespuestaRequest> requests) {
        log.info("Recibida solicitud para guardar {} respuestas", requests.size());
        List<Respuesta> respuestas = respuestaService.guardarEnLote(requests);
        return ResponseEntity.ok(respuestas);
    }

    @GetMapping("/usuario/{usuarioId}/modulo/{moduloId}")
    public ResponseEntity<List<Respuesta>> getRespuestasByUsuarioAndModulo(
            @PathVariable Integer usuarioId,
            @PathVariable Long moduloId) {
        log.info("Solicitando respuestas para usuario: {}, módulo: {}", usuarioId, moduloId);
        List<Respuesta> respuestas = respuestaService.findByUsuarioIdAndModuloId(usuarioId, moduloId);
        return ResponseEntity.ok(respuestas);
    }

    @GetMapping("/usuario/{usuarioId}/seccion/{seccionId}")
    public ResponseEntity<List<Respuesta>> getRespuestasByUsuarioAndSeccion(
            @PathVariable Integer usuarioId,
            @PathVariable Long seccionId) {
        log.info("Solicitando respuestas para usuario: {}, sección: {}", usuarioId, seccionId);
        List<Respuesta> respuestas = respuestaService.findByUsuarioIdAndSeccionId(usuarioId, seccionId);
        return ResponseEntity.ok(respuestas);
    }

    @DeleteMapping("/usuario/{usuarioId}/campo/{campoId}")
    public ResponseEntity<Void> eliminarRespuesta(
            @PathVariable Integer usuarioId,
            @PathVariable Long campoId) {
        log.info("Eliminando respuesta para usuario: {}, campo: {}", usuarioId, campoId);
        respuestaService.eliminarRespuesta(usuarioId, campoId);
        return ResponseEntity.noContent().build();
    }
}
