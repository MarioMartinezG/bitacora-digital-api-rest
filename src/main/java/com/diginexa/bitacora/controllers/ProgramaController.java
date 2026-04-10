package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.CrearProgramaRequest;
import com.diginexa.bitacora.dtos.bitacora.ProgramaDTO;
import com.diginexa.bitacora.services.ProgramaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProgramaController {

    private final ProgramaService service;

    @GetMapping
    public ResponseEntity<List<ProgramaDTO>> obtenerProgramas() {
        log.info("GET /api/programas");
        return ResponseEntity.ok(service.obtenerProgramas());
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProgramaDTO>> obtenerTodos() {
        log.info("GET /api/programas/todos");
        return ResponseEntity.ok(service.obtenerTodos());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramaDTO> crearPrograma(@RequestBody @Valid CrearProgramaRequest request) {
        log.info("POST /api/programas - nombre: {}", request.getNombre());
        return ResponseEntity.ok(service.crearPrograma(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramaDTO> actualizarPrograma(
            @PathVariable Long id,
            @RequestBody @Valid CrearProgramaRequest request) {
        log.info("PUT /api/programas/{}", id);
        return ResponseEntity.ok(service.actualizarPrograma(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarPrograma(@PathVariable Long id) {
        log.info("DELETE /api/programas/{}", id);
        service.eliminarPrograma(id);
        return ResponseEntity.noContent().build();
    }
}
