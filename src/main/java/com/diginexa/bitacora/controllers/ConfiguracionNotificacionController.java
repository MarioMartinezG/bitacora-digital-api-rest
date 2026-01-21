package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.notificacion.ConfiguracionNotificacionDTO;
import com.diginexa.bitacora.services.ConfiguracionNotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configuracion/notificaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ConfiguracionNotificacionController {

    private final ConfiguracionNotificacionService configuracionService;

    /**
     * Listar todas las configuraciones
     * GET /api/configuracion/notificaciones
     */
    @GetMapping
    public ResponseEntity<List<ConfiguracionNotificacionDTO>> listarTodas() {
        log.info("GET /api/configuracion/notificaciones");
        return ResponseEntity.ok(configuracionService.listarTodas());
    }

    /**
     * Obtener configuración por clave
     * GET /api/configuracion/notificaciones/{clave}
     */
    @GetMapping("/{clave}")
    public ResponseEntity<ConfiguracionNotificacionDTO> obtenerPorClave(@PathVariable String clave) {
        log.info("GET /api/configuracion/notificaciones/{}", clave);
        return ResponseEntity.ok(configuracionService.obtenerPorClave(clave));
    }

    /**
     * Actualizar valor de una configuración
     * PUT /api/configuracion/notificaciones/{clave}
     */
    @PutMapping("/{clave}")
    public ResponseEntity<ConfiguracionNotificacionDTO> actualizarValor(
            @PathVariable String clave,
            @RequestParam String valor) {
        log.info("PUT /api/configuracion/notificaciones/{} -> {}", clave, valor);
        return ResponseEntity.ok(configuracionService.actualizarValor(clave, valor));
    }
}
