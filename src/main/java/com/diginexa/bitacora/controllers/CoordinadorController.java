package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.coordinador.*;
import com.diginexa.bitacora.services.CoordinadorService;
import com.diginexa.bitacora.services.EstadisticasCoordinadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coordinador")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class CoordinadorController {

    private final CoordinadorService coordinadorService;
    private final EstadisticasCoordinadorService estadisticasService;

    // =============================================
    // GESTIÓN DE USUARIOS
    // =============================================

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        log.info("GET /api/coordinador/usuarios");
        return ResponseEntity.ok(coordinadorService.listarUsuarios());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDTO> obtenerUsuario(@PathVariable Integer id) {
        log.info("GET /api/coordinador/usuarios/{}", id);
        return ResponseEntity.ok(coordinadorService.obtenerUsuario(id));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDTO> crearUsuario(@Valid @RequestBody CreateUsuarioRequest request) {
        log.info("POST /api/coordinador/usuarios - correo: {}", request.getCorreo());
        UsuarioDTO created = coordinadorService.crearUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        log.info("PUT /api/coordinador/usuarios/{}", id);
        return ResponseEntity.ok(coordinadorService.actualizarUsuario(id, request));
    }

    @PatchMapping("/usuarios/{id}/toggle-activo")
    public ResponseEntity<Void> toggleUsuarioActivo(@PathVariable Integer id) {
        log.info("PATCH /api/coordinador/usuarios/{}/toggle-activo", id);
        coordinadorService.toggleUsuarioActivo(id);
        return ResponseEntity.noContent().build();
    }

    // =============================================
    // ESTADÍSTICAS Y DASHBOARD
    // =============================================

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasGeneralesDTO> obtenerEstadisticas() {
        log.info("GET /api/coordinador/estadisticas");
        return ResponseEntity.ok(estadisticasService.obtenerEstadisticasGenerales());
    }

    @GetMapping("/estadisticas/alertas")
    public ResponseEntity<List<AlertaSistemaDTO>> obtenerAlertas() {
        log.info("GET /api/coordinador/estadisticas/alertas");
        return ResponseEntity.ok(estadisticasService.obtenerAlertasPendientes());
    }

    @PatchMapping("/estadisticas/alertas/{id}/resolver")
    public ResponseEntity<Void> resolverAlerta(@PathVariable Integer id) {
        log.info("PATCH /api/coordinador/estadisticas/alertas/{}/resolver", id);
        estadisticasService.resolverAlerta(id);
        return ResponseEntity.noContent().build();
    }

    // =============================================
    // REPORTES DE PROGRESO
    // =============================================

    @GetMapping("/reportes/progreso")
    public ResponseEntity<List<ReporteProgresoDTO>> obtenerReportesProgreso() {
        log.info("GET /api/coordinador/reportes/progreso");
        return ResponseEntity.ok(estadisticasService.obtenerReportesProgreso());
    }

    // =============================================
    // GESTIÓN DE ASIGNATURAS
    // =============================================

    @GetMapping("/asignaturas")
    public ResponseEntity<List<AsignaturaDTO>> listarAsignaturas() {
        log.info("GET /api/coordinador/asignaturas");
        return ResponseEntity.ok(coordinadorService.listarAsignaturas());
    }

    @GetMapping("/asignaturas/{id}")
    public ResponseEntity<AsignaturaDTO> obtenerAsignatura(@PathVariable Integer id) {
        log.info("GET /api/coordinador/asignaturas/{}", id);
        return ResponseEntity.ok(coordinadorService.obtenerAsignatura(id));
    }

    @PostMapping("/asignaturas")
    public ResponseEntity<AsignaturaDTO> crearAsignatura(@Valid @RequestBody CreateAsignaturaRequest request) {
        log.info("POST /api/coordinador/asignaturas - codigo: {}", request.getCodigo());
        AsignaturaDTO created = coordinadorService.crearAsignatura(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/asignaturas/{id}")
    public ResponseEntity<AsignaturaDTO> actualizarAsignatura(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateAsignaturaRequest request) {
        log.info("PUT /api/coordinador/asignaturas/{}", id);
        return ResponseEntity.ok(coordinadorService.actualizarAsignatura(id, request));
    }

    @PostMapping("/asignaturas/{id}/estudiantes")
    public ResponseEntity<Void> asignarEstudiantes(
            @PathVariable Integer id,
            @RequestBody List<Integer> estudianteIds) {
        log.info("POST /api/coordinador/asignaturas/{}/estudiantes", id);
        coordinadorService.asignarEstudiantes(id, estudianteIds);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/asignaturas/{id}/tutores")
    public ResponseEntity<Void> asignarTutores(
            @PathVariable Integer id,
            @RequestBody List<Integer> tutorIds) {
        log.info("POST /api/coordinador/asignaturas/{}/tutores", id);
        coordinadorService.asignarTutores(id, tutorIds);
        return ResponseEntity.noContent().build();
    }
}
