package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.EquipoDocenteDTO;
import com.diginexa.bitacora.services.EquipoDocenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bitacora/equipo-docente")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EquipoDocenteController {

    private final EquipoDocenteService equipoService;

    /**
     * Obtener equipo docente de un usuario
     * GET /api/bitacora/equipo-docente/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<EquipoDocenteDTO>> listar(@PathVariable Integer usuarioId) {
        log.info("GET /api/bitacora/equipo-docente/usuario/{}", usuarioId);
        List<EquipoDocenteDTO> equipo = equipoService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(equipo);
    }

    /**
     * Agregar miembro al equipo
     * POST /api/bitacora/equipo-docente/usuario/{usuarioId}
     */
    @PostMapping("/usuario/{usuarioId}")
    public ResponseEntity<EquipoDocenteDTO> agregar(
            @PathVariable Integer usuarioId,
            @RequestBody @Valid EquipoDocenteDTO dto) {
        log.info("POST /api/bitacora/equipo-docente/usuario/{}", usuarioId);
        EquipoDocenteDTO creado = equipoService.agregar(usuarioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Actualizar miembro del equipo
     * PUT /api/bitacora/equipo-docente/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<EquipoDocenteDTO> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid EquipoDocenteDTO dto) {
        log.info("PUT /api/bitacora/equipo-docente/{}", id);
        EquipoDocenteDTO actualizado = equipoService.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    /**
     * Eliminar miembro del equipo
     * DELETE /api/bitacora/equipo-docente/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/bitacora/equipo-docente/{}", id);
        equipoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reordenar equipo docente
     * PUT /api/bitacora/equipo-docente/usuario/{usuarioId}/reordenar
     */
    @PutMapping("/usuario/{usuarioId}/reordenar")
    public ResponseEntity<List<EquipoDocenteDTO>> reordenar(
            @PathVariable Integer usuarioId,
            @RequestBody List<Long> ordenIds) {
        log.info("PUT /api/bitacora/equipo-docente/usuario/{}/reordenar", usuarioId);
        List<EquipoDocenteDTO> equipo = equipoService.reordenar(usuarioId, ordenIds);
        return ResponseEntity.ok(equipo);
    }
}
