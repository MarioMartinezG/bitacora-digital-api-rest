package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.CrearMetodologiaRequest;
import com.diginexa.bitacora.dtos.bitacora.MetodologiaDTO;
import com.diginexa.bitacora.services.MetodologiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metodologias")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MetodologiaController {

    private final MetodologiaService service;

    @GetMapping
    public ResponseEntity<List<MetodologiaDTO>> obtenerActivas() {
        log.info("GET /api/metodologias");
        return ResponseEntity.ok(service.listarActivas());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MetodologiaDTO>> obtenerTodas() {
        log.info("GET /api/metodologias/todos");
        return ResponseEntity.ok(service.listarTodas());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetodologiaDTO> crear(@RequestBody @Valid CrearMetodologiaRequest request) {
        log.info("POST /api/metodologias - label: {}", request.getLabel());
        return ResponseEntity.ok(service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetodologiaDTO> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid CrearMetodologiaRequest request) {
        log.info("PUT /api/metodologias/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetodologiaDTO> toggleActivo(@PathVariable Long id) {
        log.info("PATCH /api/metodologias/{}/toggle", id);
        return ResponseEntity.ok(service.toggleActivo(id));
    }
}
