package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.CrearTecnicaRequest;
import com.diginexa.bitacora.dtos.bitacora.TecnicaDTO;
import com.diginexa.bitacora.services.TecnicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tecnicas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TecnicaController {

    private final TecnicaService service;

    @GetMapping
    public ResponseEntity<List<TecnicaDTO>> obtenerActivas() {
        log.info("GET /api/tecnicas");
        return ResponseEntity.ok(service.listarActivas());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TecnicaDTO>> obtenerTodas() {
        log.info("GET /api/tecnicas/todos");
        return ResponseEntity.ok(service.listarTodas());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TecnicaDTO> crear(@RequestBody @Valid CrearTecnicaRequest request) {
        log.info("POST /api/tecnicas - label: {}, grupo: {}", request.getLabel(), request.getGrupo());
        return ResponseEntity.ok(service.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TecnicaDTO> actualizar(
            @PathVariable Long id,
            @RequestBody @Valid CrearTecnicaRequest request) {
        log.info("PUT /api/tecnicas/{}", id);
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TecnicaDTO> toggleActivo(@PathVariable Long id) {
        log.info("PATCH /api/tecnicas/{}/toggle", id);
        return ResponseEntity.ok(service.toggleActivo(id));
    }
}
