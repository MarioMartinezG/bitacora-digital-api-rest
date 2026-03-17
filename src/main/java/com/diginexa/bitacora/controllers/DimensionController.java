package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.CrearDimensionRequest;
import com.diginexa.bitacora.dtos.bitacora.DimensionDTO;
import com.diginexa.bitacora.services.DimensionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dimensiones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DimensionController {

    private final DimensionService service;

    @GetMapping
    public ResponseEntity<List<DimensionDTO>> obtenerActivas() {
        log.info("GET /api/dimensiones");
        return ResponseEntity.ok(service.listarActivas());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DimensionDTO>> obtenerTodas() {
        log.info("GET /api/dimensiones/todos");
        return ResponseEntity.ok(service.listarTodas());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DimensionDTO> crear(@RequestBody @Valid CrearDimensionRequest request) {
        log.info("POST /api/dimensiones - nombre: {}", request.getNombre());
        return ResponseEntity.ok(service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DimensionDTO> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid CrearDimensionRequest request) {
        log.info("PUT /api/dimensiones/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DimensionDTO> toggleActivo(@PathVariable Long id) {
        log.info("PATCH /api/dimensiones/{}/toggle", id);
        return ResponseEntity.ok(service.toggleActivo(id));
    }
}
