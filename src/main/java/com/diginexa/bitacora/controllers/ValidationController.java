package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.validation.ValidateActivityRequest;
import com.diginexa.bitacora.dtos.validation.ValidateEvaluationRequest;
import com.diginexa.bitacora.dtos.validation.ValidationResponse;
import com.diginexa.bitacora.services.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ValidationController {

    private final ValidationService validationService;

    @PostMapping("/activity")
    public ResponseEntity<ValidationResponse> validateActivity(@RequestBody ValidateActivityRequest request) {
        log.info("POST /api/validation/activity - dimension: {}", request.getDimension());
        ValidationResponse response = validationService.validateActivity(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/evaluation")
    public ResponseEntity<ValidationResponse> validateEvaluation(@RequestBody ValidateEvaluationRequest request) {
        log.info("POST /api/validation/evaluation - actividad: {}", request.getNombreActividad());
        ValidationResponse response = validationService.validateEvaluation(request);
        return ResponseEntity.ok(response);
    }
}
