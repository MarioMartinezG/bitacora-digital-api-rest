package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.notificacion.AsignarTutorRequest;
import com.diginexa.bitacora.dtos.notificacion.TutorEstudianteDTO;
import com.diginexa.bitacora.services.TutorEstudianteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutor-estudiante")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TutorEstudianteController {

    private final TutorEstudianteService tutorEstudianteService;

    /**
     * Asignar un tutor a un estudiante
     * POST /api/tutor-estudiante/asignar
     */
    @PostMapping("/asignar")
    public ResponseEntity<TutorEstudianteDTO> asignarTutor(
            @Valid @RequestBody AsignarTutorRequest request) {
        log.info("POST /api/tutor-estudiante/asignar - tutor: {}, estudiante: {}",
                request.getTutorId(), request.getEstudianteId());
        TutorEstudianteDTO resultado = tutorEstudianteService.asignarTutor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    /**
     * Obtener estudiantes asignados a un tutor
     * GET /api/tutor-estudiante/tutor/{tutorId}/estudiantes
     */
    @GetMapping("/tutor/{tutorId}/estudiantes")
    public ResponseEntity<List<TutorEstudianteDTO>> obtenerEstudiantesPorTutor(
            @PathVariable Integer tutorId) {
        log.info("GET /api/tutor-estudiante/tutor/{}/estudiantes", tutorId);
        return ResponseEntity.ok(tutorEstudianteService.obtenerEstudiantesPorTutor(tutorId));
    }

    /**
     * Obtener tutor asignado a un estudiante
     * GET /api/tutor-estudiante/estudiante/{estudianteId}/tutor
     */
    @GetMapping("/estudiante/{estudianteId}/tutor")
    public ResponseEntity<TutorEstudianteDTO> obtenerTutorPorEstudiante(
            @PathVariable Integer estudianteId) {
        log.info("GET /api/tutor-estudiante/estudiante/{}/tutor", estudianteId);
        return ResponseEntity.ok(tutorEstudianteService.obtenerTutorPorEstudiante(estudianteId));
    }

    /**
     * Verificar si un estudiante tiene tutor asignado
     * GET /api/tutor-estudiante/estudiante/{estudianteId}/tiene-tutor
     */
    @GetMapping("/estudiante/{estudianteId}/tiene-tutor")
    public ResponseEntity<Boolean> tieneTutorAsignado(@PathVariable Integer estudianteId) {
        log.info("GET /api/tutor-estudiante/estudiante/{}/tiene-tutor", estudianteId);
        return ResponseEntity.ok(tutorEstudianteService.tieneTutorAsignado(estudianteId));
    }

    /**
     * Desactivar una asignación tutor-estudiante
     * DELETE /api/tutor-estudiante/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivarAsignacion(@PathVariable Long id) {
        log.info("DELETE /api/tutor-estudiante/{}", id);
        tutorEstudianteService.desactivarAsignacion(id);
        return ResponseEntity.noContent().build();
    }
}
