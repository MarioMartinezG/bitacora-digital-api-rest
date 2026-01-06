package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.TemaContenidoDTO;
import com.diginexa.bitacora.services.TemaContenidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bitacora/temas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TemaContenidoController {

    private final TemaContenidoService temaService;

    /**
     * Obtener todos los temas de un usuario
     * GET /api/bitacora/temas/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<TemaContenidoDTO>> listar(@PathVariable Integer usuarioId) {
        log.info("GET /api/bitacora/temas/usuario/{}", usuarioId);
        List<TemaContenidoDTO> temas = temaService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(temas);
    }

    /**
     * Crear tema
     * POST /api/bitacora/temas/usuario/{usuarioId}
     */
    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<TemaContenidoDTO> crear(
            @PathVariable Integer usuarioId,
            @RequestBody @Valid TemaContenidoDTO dto) {
        log.info("POST /api/bitacora/temas/usuario/{}", usuarioId);
        TemaContenidoDTO creado = temaService.crear(usuarioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Actualizar tema (incluyendo subtemas)
     * PUT /api/bitacora/temas/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TemaContenidoDTO> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid TemaContenidoDTO dto) {
        log.info("PUT /api/bitacora/temas/{}", id);
        TemaContenidoDTO actualizado = temaService.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Eliminar tema
     * DELETE /api/bitacora/temas/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/bitacora/temas/{}", id);
        temaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Agregar subtema a un tema
     * POST /api/bitacora/temas/{temaId}/subtemas
     */
    @PostMapping("/{temaId}/subtemas")
    public ResponseEntity<TemaContenidoDTO> agregarSubtema(
            @PathVariable Long temaId,
            @RequestBody String subtema) {
        log.info("POST /api/bitacora/temas/{}/subtemas", temaId);
        TemaContenidoDTO actualizado = temaService.agregarSubtema(temaId, subtema);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Eliminar subtema de un tema
     * DELETE /api/bitacora/temas/{temaId}/subtemas/{indice}
     */
    @DeleteMapping("/{temaId}/subtemas/{indice}")
    public ResponseEntity<TemaContenidoDTO> eliminarSubtema(
            @PathVariable Long temaId,
            @PathVariable Integer indice) {
        log.info("DELETE /api/bitacora/temas/{}/subtemas/{}", temaId, indice);
        TemaContenidoDTO actualizado = temaService.eliminarSubtema(temaId, indice);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Actualizar subtema de un tema
     * PUT /api/bitacora/temas/{temaId}/subtemas/{indice}
     */
    @PutMapping("/{temaId}/subtemas/{indice}")
    public ResponseEntity<TemaContenidoDTO> actualizarSubtema(
            @PathVariable Long temaId,
            @PathVariable Integer indice,
            @RequestBody String nuevoSubtema) {
        log.info("PUT /api/bitacora/temas/{}/subtemas/{}", temaId, indice);
        TemaContenidoDTO actualizado = temaService.actualizarSubtema(temaId, indice, nuevoSubtema);
        return ResponseEntity.ok(actualizado);
    }
}
