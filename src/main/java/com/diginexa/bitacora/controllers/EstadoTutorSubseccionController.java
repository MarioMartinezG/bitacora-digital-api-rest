package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.ActualizarEstadoTutorSubseccionRequest;
import com.diginexa.bitacora.dtos.bitacora.EstadoTutorSubseccionDTO;
import com.diginexa.bitacora.services.EstadoTutorSubseccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estado-tutor-subseccion")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EstadoTutorSubseccionController {

    private final EstadoTutorSubseccionService service;

    @PatchMapping
    @PreAuthorize("hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<EstadoTutorSubseccionDTO> actualizarEstado(
            @RequestBody @Valid ActualizarEstadoTutorSubseccionRequest request,
            Authentication authentication) {
        log.info("PATCH /api/estado-tutor-subseccion - estudiante: {}, seccion: {}/{}, estado: {}",
                request.getEstudianteId(), request.getSeccionCodigo(), request.getSubseccionCodigo(), request.getEstado());

        com.diginexa.bitacora.entities.Usuario usuario =
                (com.diginexa.bitacora.entities.Usuario) authentication.getPrincipal();

        EstadoTutorSubseccionDTO resultado = service.actualizarEstado(usuario.getId(), request);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/estudiante/{estudianteId}/seccion/{seccionCodigo}")
    public ResponseEntity<List<EstadoTutorSubseccionDTO>> obtenerEstadosPorSeccion(
            @PathVariable Integer estudianteId,
            @PathVariable String seccionCodigo) {
        log.info("GET /api/estado-tutor-subseccion/estudiante/{}/seccion/{}", estudianteId, seccionCodigo);
        return ResponseEntity.ok(service.obtenerEstadosPorSeccion(estudianteId, seccionCodigo));
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<EstadoTutorSubseccionDTO>> obtenerEstadosPorEstudiante(
            @PathVariable Integer estudianteId) {
        log.info("GET /api/estado-tutor-subseccion/estudiante/{}", estudianteId);
        return ResponseEntity.ok(service.obtenerEstadosPorEstudiante(estudianteId));
    }
}
