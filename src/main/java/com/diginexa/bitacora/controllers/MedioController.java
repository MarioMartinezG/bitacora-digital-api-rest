package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.CrearMedioRequest;
import com.diginexa.bitacora.dtos.bitacora.MedioDTO;
import com.diginexa.bitacora.services.MedioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medios")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MedioController {

    private final MedioService service;

    @GetMapping
    public ResponseEntity<List<MedioDTO>> obtenerActivos() {
        log.info("GET /api/medios");
        return ResponseEntity.ok(service.obtenerActivos());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MedioDTO>> obtenerTodos() {
        log.info("GET /api/medios/todos");
        return ResponseEntity.ok(service.obtenerTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedioDTO> crearMedio(@RequestBody @Valid CrearMedioRequest request) {
        log.info("POST /api/medios - label: {}, categoria: {}", request.getLabel(), request.getCategoria());
        return ResponseEntity.ok(service.crearMedio(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedioDTO> actualizarMedio(
            @PathVariable Long id,
            @RequestBody @Valid CrearMedioRequest request) {
        log.info("PUT /api/medios/{}", id);
        return ResponseEntity.ok(service.actualizarMedio(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedioDTO> toggleActivo(@PathVariable Long id) {
        log.info("PATCH /api/medios/{}/toggle", id);
        return ResponseEntity.ok(service.toggleActivo(id));
    }
}
