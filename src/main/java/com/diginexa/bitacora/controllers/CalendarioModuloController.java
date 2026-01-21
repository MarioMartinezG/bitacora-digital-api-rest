package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.notificacion.CalendarioModuloDTO;
import com.diginexa.bitacora.services.CalendarioModuloService;
import com.diginexa.bitacora.services.VencimientoSchedulerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendario")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CalendarioModuloController {

    private final CalendarioModuloService calendarioService;
    private final VencimientoSchedulerService schedulerService;

    /**
     * Listar todos los calendarios activos
     * GET /api/calendario
     */
    @GetMapping
    public ResponseEntity<List<CalendarioModuloDTO>> listarTodos() {
        log.info("GET /api/calendario");
        return ResponseEntity.ok(calendarioService.listarTodos());
    }

    /**
     * Obtener calendario por sección
     * GET /api/calendario/seccion/{seccionCodigo}
     */
    @GetMapping("/seccion/{seccionCodigo}")
    public ResponseEntity<CalendarioModuloDTO> obtenerPorSeccion(@PathVariable String seccionCodigo) {
        log.info("GET /api/calendario/seccion/{}", seccionCodigo);
        return ResponseEntity.ok(calendarioService.obtenerPorSeccion(seccionCodigo));
    }

    /**
     * Obtener calendario por ID
     * GET /api/calendario/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CalendarioModuloDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/calendario/{}", id);
        return ResponseEntity.ok(calendarioService.obtenerPorId(id));
    }

    /**
     * Crear nuevo calendario
     * POST /api/calendario
     */
    @PostMapping
    public ResponseEntity<CalendarioModuloDTO> crear(@Valid @RequestBody CalendarioModuloDTO dto) {
        log.info("POST /api/calendario - sección: {}", dto.getSeccionCodigo());
        CalendarioModuloDTO creado = calendarioService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /**
     * Actualizar calendario existente
     * PUT /api/calendario/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CalendarioModuloDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CalendarioModuloDTO dto) {
        log.info("PUT /api/calendario/{}", id);
        return ResponseEntity.ok(calendarioService.actualizar(id, dto));
    }

    /**
     * Eliminar calendario
     * DELETE /api/calendario/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/calendario/{}", id);
        calendarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener próximos vencimientos
     * GET /api/calendario/proximos?dias=7
     */
    @GetMapping("/proximos")
    public ResponseEntity<List<CalendarioModuloDTO>> obtenerProximos(
            @RequestParam(defaultValue = "7") int dias) {
        log.info("GET /api/calendario/proximos?dias={}", dias);
        return ResponseEntity.ok(calendarioService.obtenerProximosVencimientos(dias));
    }

    /**
     * Ejecutar verificación de vencimientos manualmente
     * POST /api/calendario/verificar-vencimientos
     */
    @PostMapping("/verificar-vencimientos")
    public ResponseEntity<Void> verificarVencimientosManual() {
        log.info("POST /api/calendario/verificar-vencimientos");
        schedulerService.ejecutarVerificacionManual();
        return ResponseEntity.ok().build();
    }
}
