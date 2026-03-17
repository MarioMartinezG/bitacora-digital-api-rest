package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.CrearInstrumentoRequest;
import com.diginexa.bitacora.dtos.bitacora.InstrumentoDTO;
import com.diginexa.bitacora.services.InstrumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instrumentos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InstrumentoController {

    private final InstrumentoService service;

    @GetMapping
    public ResponseEntity<List<InstrumentoDTO>> obtenerActivos() {
        log.info("GET /api/instrumentos");
        return ResponseEntity.ok(service.listarActivos());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InstrumentoDTO>> obtenerTodos() {
        log.info("GET /api/instrumentos/todos");
        return ResponseEntity.ok(service.listarTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstrumentoDTO> crear(@RequestBody @Valid CrearInstrumentoRequest request) {
        log.info("POST /api/instrumentos - label: {}", request.getLabel());
        return ResponseEntity.ok(service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstrumentoDTO> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid CrearInstrumentoRequest request) {
        log.info("PUT /api/instrumentos/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InstrumentoDTO> toggleActivo(@PathVariable Long id) {
        log.info("PATCH /api/instrumentos/{}/toggle", id);
        return ResponseEntity.ok(service.toggleActivo(id));
    }
}
