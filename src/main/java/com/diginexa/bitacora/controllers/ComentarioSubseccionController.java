package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.bitacora.ComentarioSubseccionDTO;
import com.diginexa.bitacora.dtos.bitacora.CrearComentarioSubseccionRequest;
import com.diginexa.bitacora.services.ComentarioSubseccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comentarios-subseccion")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ComentarioSubseccionController {

    private final ComentarioSubseccionService service;

    @PostMapping
    @PreAuthorize("hasRole('TUTOR') or hasRole('ADMIN')")
    public ResponseEntity<ComentarioSubseccionDTO> crearComentario(
            @RequestBody @Valid CrearComentarioSubseccionRequest request,
            Authentication authentication) {
        log.info("POST /api/comentarios-subseccion - estudiante: {}, seccion: {}/{}",
                request.getEstudianteId(), request.getSeccionCodigo(), request.getSubseccionCodigo());

        com.diginexa.bitacora.entities.Usuario usuario =
                (com.diginexa.bitacora.entities.Usuario) authentication.getPrincipal();

        ComentarioSubseccionDTO resultado = service.crearComentario(usuario.getId(), request);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/estudiante/{estudianteId}/seccion/{seccionCodigo}/subseccion/{subseccionCodigo}")
    public ResponseEntity<List<ComentarioSubseccionDTO>> obtenerComentarios(
            @PathVariable Integer estudianteId,
            @PathVariable String seccionCodigo,
            @PathVariable String subseccionCodigo) {
        log.info("GET /api/comentarios-subseccion/estudiante/{}/seccion/{}/subseccion/{}",
                estudianteId, seccionCodigo, subseccionCodigo);
        return ResponseEntity.ok(service.obtenerComentarios(estudianteId, seccionCodigo, subseccionCodigo));
    }

    @GetMapping("/estudiante/{estudianteId}/seccion/{seccionCodigo}")
    public ResponseEntity<List<ComentarioSubseccionDTO>> obtenerComentariosPorSeccion(
            @PathVariable Integer estudianteId,
            @PathVariable String seccionCodigo) {
        log.info("GET /api/comentarios-subseccion/estudiante/{}/seccion/{}", estudianteId, seccionCodigo);
        return ResponseEntity.ok(service.obtenerComentariosPorSeccion(estudianteId, seccionCodigo));
    }

    @GetMapping("/estudiante/{estudianteId}/seccion/{seccionCodigo}/conteo")
    public ResponseEntity<Map<String, Long>> obtenerConteoPorSeccion(
            @PathVariable Integer estudianteId,
            @PathVariable String seccionCodigo) {
        log.info("GET /api/comentarios-subseccion/estudiante/{}/seccion/{}/conteo", estudianteId, seccionCodigo);
        return ResponseEntity.ok(service.contarComentariosPorSeccion(estudianteId, seccionCodigo));
    }
}
