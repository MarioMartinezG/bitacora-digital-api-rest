package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.notificacion.CrearSolicitudSesionRequest;
import com.diginexa.bitacora.dtos.notificacion.ResponderSolicitudRequest;
import com.diginexa.bitacora.dtos.notificacion.SolicitudSesionDTO;
import com.diginexa.bitacora.services.SolicitudSesionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes-sesion")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SolicitudSesionController {

    private final SolicitudSesionService solicitudService;

    /**
     * Crear una nueva solicitud de sesión
     * POST /api/solicitudes-sesion
     */
    @PostMapping
    public ResponseEntity<SolicitudSesionDTO> crearSolicitud(
            @Valid @RequestBody CrearSolicitudSesionRequest request) {
        log.info("POST /api/solicitudes-sesion - estudiante: {}", request.getEstudianteId());
        SolicitudSesionDTO creada = solicitudService.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Obtener solicitudes de un estudiante
     * GET /api/solicitudes-sesion/estudiante/{estudianteId}
     */
    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<SolicitudSesionDTO>> obtenerPorEstudiante(
            @PathVariable Integer estudianteId) {
        log.info("GET /api/solicitudes-sesion/estudiante/{}", estudianteId);
        return ResponseEntity.ok(solicitudService.obtenerPorEstudiante(estudianteId));
    }

    /**
     * Obtener solicitudes de un tutor
     * GET /api/solicitudes-sesion/tutor/{tutorId}
     */
    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<SolicitudSesionDTO>> obtenerPorTutor(
            @PathVariable Integer tutorId) {
        log.info("GET /api/solicitudes-sesion/tutor/{}", tutorId);
        return ResponseEntity.ok(solicitudService.obtenerPorTutor(tutorId));
    }

    /**
     * Obtener solicitudes pendientes de un tutor
     * GET /api/solicitudes-sesion/tutor/{tutorId}/pendientes
     */
    @GetMapping("/tutor/{tutorId}/pendientes")
    public ResponseEntity<List<SolicitudSesionDTO>> obtenerPendientesPorTutor(
            @PathVariable Integer tutorId) {
        log.info("GET /api/solicitudes-sesion/tutor/{}/pendientes", tutorId);
        return ResponseEntity.ok(solicitudService.obtenerPendientesPorTutor(tutorId));
    }

    /**
     * Obtener una solicitud por ID
     * GET /api/solicitudes-sesion/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SolicitudSesionDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/solicitudes-sesion/{}", id);
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }

    /**
     * Responder a una solicitud (tutor)
     * PUT /api/solicitudes-sesion/{id}/responder
     */
    @PutMapping("/{id}/responder")
    public ResponseEntity<SolicitudSesionDTO> responderSolicitud(
            @PathVariable Long id,
            @Valid @RequestBody ResponderSolicitudRequest request) {
        log.info("PUT /api/solicitudes-sesion/{}/responder - estado: {}", id, request.getEstado());
        return ResponseEntity.ok(solicitudService.responderSolicitud(id, request));
    }

    /**
     * Cancelar una solicitud (estudiante)
     * DELETE /api/solicitudes-sesion/{id}/estudiante/{estudianteId}
     */
    @DeleteMapping("/{id}/estudiante/{estudianteId}")
    public ResponseEntity<Void> cancelarSolicitud(
            @PathVariable Long id,
            @PathVariable Integer estudianteId) {
        log.info("DELETE /api/solicitudes-sesion/{}/estudiante/{}", id, estudianteId);
        solicitudService.cancelarSolicitud(id, estudianteId);
        return ResponseEntity.noContent().build();
    }
}
