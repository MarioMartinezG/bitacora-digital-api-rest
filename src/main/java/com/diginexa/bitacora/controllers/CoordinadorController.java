package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.coordinador.*;
import com.diginexa.bitacora.services.CoordinadorService;
import com.diginexa.bitacora.services.EstadisticasCoordinadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/usuarios/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportarUsuariosResponse> importarUsuariosCsv(
            @RequestParam("archivo") MultipartFile archivo) {
        log.info("POST /api/coordinador/usuarios/importar - archivo: {}", archivo.getOriginalFilename());
        return ResponseEntity.ok(coordinadorService.importarUsuariosCsv(archivo));
    }

    @PatchMapping("/usuarios/{id}/toggle-activo")
    public ResponseEntity<Void> toggleUsuarioActivo(@PathVariable Integer id) {
        log.info("PATCH /api/coordinador/usuarios/{}/toggle-activo", id);
        coordinadorService.toggleUsuarioActivo(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/usuarios/{id}/marcar-graduado")
    public ResponseEntity<Void> marcarUsuarioGraduado(@PathVariable Integer id) {
        log.info("PATCH /api/coordinador/usuarios/{}/marcar-graduado", id);
        coordinadorService.marcarUsuarioGraduado(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/usuarios/{id}/reactivar")
    public ResponseEntity<Void> reactivarUsuario(@PathVariable Integer id) {
        log.info("PATCH /api/coordinador/usuarios/{}/reactivar", id);
        coordinadorService.reactivarUsuario(id);
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

}
