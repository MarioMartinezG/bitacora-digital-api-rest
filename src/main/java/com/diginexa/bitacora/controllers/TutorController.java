package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.tutor.TutorAskRequest;
import com.diginexa.bitacora.dtos.tutor.TutorAskResponse;
import com.diginexa.bitacora.dtos.tutor.TutorHealthResponse;
import com.diginexa.bitacora.dtos.tutor.TutorStatusResponse;
import com.diginexa.bitacora.services.TutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tutor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TutorController {

    private final TutorService tutorService;

    @GetMapping("/status")
    public ResponseEntity<TutorStatusResponse> getStatus() {
        log.info("GET /api/tutor/status - Consultando estado del tutor");
        TutorStatusResponse response = tutorService.getStatus();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<TutorHealthResponse> getHealth() {
        log.info("GET /api/tutor/health - Verificando salud del tutor");
        TutorHealthResponse response = tutorService.getHealth();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/modules")
    public ResponseEntity<List<Map<String, Object>>> getModules() {
        log.info("GET /api/tutor/modules - Obteniendo modulos del curso");
        List<Map<String, Object>> modules = tutorService.getModules();
        return ResponseEntity.ok(modules);
    }

    @PostMapping("/ask")
    public ResponseEntity<TutorAskResponse> ask(@Valid @RequestBody TutorAskRequest request) {
        log.info("POST /api/tutor/ask - Pregunta: '{}' en modulo: '{}'",
                request.getQuestion().substring(0, Math.min(50, request.getQuestion().length())),
                request.getModule());
        TutorAskResponse response = tutorService.ask(request);
        return ResponseEntity.ok(response);
    }
}
