package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.ActualizarEstadoProfesorRequest;
import com.diginexa.bitacora.dtos.bitacora.ProgresoUsuarioDTO;
import com.diginexa.bitacora.services.ProgresoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bitacora/progreso")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProgresoController {

    private final ProgresoService progresoService;

    /**
     * Obtener progreso completo de un usuario
     * GET /api/bitacora/progreso/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ProgresoUsuarioDTO> obtenerProgreso(@PathVariable Integer usuarioId) {
        log.info("GET /api/bitacora/progreso/usuario/{}", usuarioId);
        ProgresoUsuarioDTO progreso = progresoService.obtenerProgresoCompleto(usuarioId);
        return ResponseEntity.ok(progreso);
    }

    /**
     * Obtener progreso de una sección específica
     * GET /api/bitacora/progreso/usuario/{usuarioId}/seccion/{seccionCodigo}
     */
    @GetMapping("/usuario/{usuarioId}/seccion/{seccionCodigo}")
    public ResponseEntity<ProgresoUsuarioDTO.ProgresoSeccionDTO> obtenerProgresoSeccion(
            @PathVariable Integer usuarioId,
            @PathVariable String seccionCodigo) {
        log.info("GET /api/bitacora/progreso/usuario/{}/seccion/{}", usuarioId, seccionCodigo);
        ProgresoUsuarioDTO.ProgresoSeccionDTO progreso =
            progresoService.obtenerProgresoSeccion(usuarioId, seccionCodigo);
        return ResponseEntity.ok(progreso);
    }

    /**
     * Actualizar estado de una sección
     * PUT /api/bitacora/progreso/usuario/{usuarioId}/seccion/{seccionCodigo}
     */
    @PutMapping("/usuario/{usuarioId}/seccion/{seccionCodigo}")
    public ResponseEntity<ProgresoUsuarioDTO.ProgresoSeccionDTO> actualizarEstado(
            @PathVariable Integer usuarioId,
            @PathVariable String seccionCodigo,
            @RequestParam String estado) {
        log.info("PUT /api/bitacora/progreso/usuario/{}/seccion/{} -> {}",
                 usuarioId, seccionCodigo, estado);
        ProgresoUsuarioDTO.ProgresoSeccionDTO progreso =
            progresoService.actualizarEstadoSeccion(usuarioId, seccionCodigo, estado, null);
        return ResponseEntity.ok(progreso);
    }

    /**
     * Actualizar estado asignado por profesor/tutor.
     * Este estado tiene prioridad sobre el estado calculado automáticamente.
     * Solo accesible por usuarios con rol 'tutor' o 'admin'.
     *
     * PATCH /api/bitacora/progreso/estado-profesor
     */
    @PatchMapping("/estado-profesor")
    @PreAuthorize("hasRole('tutor') or hasRole('admin')")
    public ResponseEntity<ProgresoUsuarioDTO.ProgresoSeccionDTO> actualizarEstadoProfesor(
            @RequestBody @Valid ActualizarEstadoProfesorRequest request) {
        log.info("PATCH /api/bitacora/progreso/estado-profesor - Estudiante: {}, Sección: {}, Estado: {}",
                 request.getEstudianteId(), request.getSeccionCodigo(), request.getEstadoProfesor());

        ProgresoUsuarioDTO.ProgresoSeccionDTO progreso = progresoService.actualizarEstadoProfesor(
            request.getEstudianteId(),
            request.getSeccionCodigo(),
            request.getEstadoProfesor()
        );

        return ResponseEntity.ok(progreso);
    }

    /**
     * Limpiar estado asignado por profesor (volver al estado calculado).
     * Solo accesible por usuarios con rol 'tutor' o 'admin'.
     *
     * DELETE /api/bitacora/progreso/estado-profesor/usuario/{estudianteId}/seccion/{seccionCodigo}
     */
    @DeleteMapping("/estado-profesor/usuario/{estudianteId}/seccion/{seccionCodigo}")
    @PreAuthorize("hasRole('tutor') or hasRole('admin')")
    public ResponseEntity<ProgresoUsuarioDTO.ProgresoSeccionDTO> limpiarEstadoProfesor(
            @PathVariable Integer estudianteId,
            @PathVariable String seccionCodigo) {
        log.info("DELETE /api/bitacora/progreso/estado-profesor - Estudiante: {}, Sección: {}",
                 estudianteId, seccionCodigo);

        ProgresoUsuarioDTO.ProgresoSeccionDTO progreso = progresoService.limpiarEstadoProfesor(
            estudianteId,
            seccionCodigo
        );

        return ResponseEntity.ok(progreso);
    }
}
