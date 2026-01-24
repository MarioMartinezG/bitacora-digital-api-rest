package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.tutor.TutorAskRequest;
import com.diginexa.bitacora.dtos.tutor.TutorAskResponse;
import com.diginexa.bitacora.dtos.tutor.TutorHealthResponse;
import com.diginexa.bitacora.dtos.tutor.TutorStatusResponse;
import com.diginexa.bitacora.dtos.tutor.DocumentListResponse;
import com.diginexa.bitacora.dtos.tutor.IndexTaskResponse;
import com.diginexa.bitacora.dtos.tutor.IndexStatusResponse;
import com.diginexa.bitacora.dtos.tutor.DocumentUploadResponse;
import com.diginexa.bitacora.services.TutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/documents")
    public ResponseEntity<DocumentListResponse> getDocuments() {
        log.info("GET /api/tutor/documents - Obteniendo lista de documentos");
        DocumentListResponse response = tutorService.getDocuments();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/index")
    public ResponseEntity<IndexTaskResponse> startIndexing() {
        log.info("POST /api/tutor/index - Iniciando indexacion de documentos");
        IndexTaskResponse response = tutorService.startIndexing();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/index/status/{taskId}")
    public ResponseEntity<IndexStatusResponse> getIndexStatus(@PathVariable String taskId) {
        log.info("GET /api/tutor/index/status/{} - Consultando estado de indexacion", taskId);
        IndexStatusResponse response = tutorService.getIndexStatus(taskId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentUploadResponse> uploadDocuments(
            @RequestParam("files") List<MultipartFile> files) {
        log.info("POST /api/tutor/documents/upload - Subiendo {} archivos", files.size());
        DocumentUploadResponse response = tutorService.uploadDocuments(files);
        return ResponseEntity.ok(response);
    }
}
