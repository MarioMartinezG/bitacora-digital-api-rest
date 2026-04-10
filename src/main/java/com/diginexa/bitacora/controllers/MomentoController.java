package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.CrearMomentoRequest;
import com.diginexa.bitacora.dtos.bitacora.MomentoDTO;
import com.diginexa.bitacora.services.MomentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/momentos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MomentoController {

    private final MomentoService service;

    @GetMapping
    public ResponseEntity<List<MomentoDTO>> obtenerMomentos() {
        log.info("GET /api/momentos");
        return ResponseEntity.ok(service.obtenerMomentos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MomentoDTO> obtenerMomento(@PathVariable Long id) {
        log.info("GET /api/momentos/{}", id);
        return ResponseEntity.ok(service.obtenerMomento(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MomentoDTO> crearMomento(@RequestBody @Valid CrearMomentoRequest request) {
        log.info("POST /api/momentos - nombre: {}", request.getNombre());
        return ResponseEntity.ok(service.crearMomento(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MomentoDTO> actualizarMomento(
            @PathVariable Long id,
            @RequestBody @Valid CrearMomentoRequest request) {
        log.info("PUT /api/momentos/{}", id);
        return ResponseEntity.ok(service.actualizarMomento(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarMomento(@PathVariable Long id) {
        log.info("DELETE /api/momentos/{}", id);
        service.eliminarMomento(id);
        return ResponseEntity.noContent().build();
    }
}
