package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.notificacion.NotificacionDTO;
import com.diginexa.bitacora.dtos.notificacion.NotificacionResumenDTO;
import com.diginexa.bitacora.services.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final NotificacionService notificacionService;

    /**
     * Obtener todas las notificaciones de un usuario
     * GET /api/notificaciones/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<NotificacionDTO>> obtenerNotificaciones(
            @PathVariable Integer usuarioId) {
        log.info("GET /api/notificaciones/usuario/{}", usuarioId);
        return ResponseEntity.ok(notificacionService.obtenerNotificaciones(usuarioId));
    }

    /**
     * Obtener notificaciones no leídas de un usuario
     * GET /api/notificaciones/usuario/{usuarioId}/no-leidas
     */
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public ResponseEntity<List<NotificacionDTO>> obtenerNoLeidas(
            @PathVariable Integer usuarioId) {
        log.info("GET /api/notificaciones/usuario/{}/no-leidas", usuarioId);
        return ResponseEntity.ok(notificacionService.obtenerNoLeidas(usuarioId));
    }

    /**
     * Obtener resumen de notificaciones (conteo por prioridad)
     * GET /api/notificaciones/usuario/{usuarioId}/resumen
     */
    @GetMapping("/usuario/{usuarioId}/resumen")
    public ResponseEntity<NotificacionResumenDTO> obtenerResumen(
            @PathVariable Integer usuarioId) {
        log.info("GET /api/notificaciones/usuario/{}/resumen", usuarioId);
        return ResponseEntity.ok(notificacionService.obtenerResumen(usuarioId));
    }

    /**
     * Obtener notificaciones por tipo
     * GET /api/notificaciones/usuario/{usuarioId}/tipo/{tipo}
     */
    @GetMapping("/usuario/{usuarioId}/tipo/{tipo}")
    public ResponseEntity<List<NotificacionDTO>> obtenerPorTipo(
            @PathVariable Integer usuarioId,
            @PathVariable String tipo) {
        log.info("GET /api/notificaciones/usuario/{}/tipo/{}", usuarioId, tipo);
        return ResponseEntity.ok(notificacionService.obtenerPorTipo(usuarioId, tipo));
    }

    /**
     * Obtener una notificación por ID
     * GET /api/notificaciones/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificacionDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/notificaciones/{}", id);
        return ResponseEntity.ok(notificacionService.obtenerPorId(id));
    }

    /**
     * Marcar una notificación como leída
     * PUT /api/notificaciones/{id}/leer
     */
    @PutMapping("/{id}/leer")
    public ResponseEntity<NotificacionDTO> marcarComoLeida(@PathVariable Long id) {
        log.info("PUT /api/notificaciones/{}/leer", id);
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
    }

    /**
     * Marcar todas las notificaciones de un usuario como leídas
     * PUT /api/notificaciones/usuario/{usuarioId}/leer-todas
     */
    @PutMapping("/usuario/{usuarioId}/leer-todas")
    public ResponseEntity<Void> marcarTodasComoLeidas(@PathVariable Integer usuarioId) {
        log.info("PUT /api/notificaciones/usuario/{}/leer-todas", usuarioId);
        notificacionService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok().build();
    }

    /**
     * Eliminar una notificación
     * DELETE /api/notificaciones/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/notificaciones/{}", id);
        notificacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
