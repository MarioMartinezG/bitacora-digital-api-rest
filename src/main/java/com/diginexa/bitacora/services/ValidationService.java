package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.validation.ValidateActivityRequest;
import com.diginexa.bitacora.dtos.validation.ValidateEvaluationRequest;
import com.diginexa.bitacora.dtos.validation.ValidationResponse;
import com.diginexa.bitacora.exceptions.domain.TutorServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final WebClient tutorWebClient;

    public ValidationResponse validateActivity(ValidateActivityRequest request) {
        log.info("Validando coherencia de actividad de aprendizaje - dimension: {}", request.getDimension());
        try {
            return tutorWebClient.post()
                    .uri("/validate-activity")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ValidationResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("Error de conexion con el servicio de validacion: {}", e.getMessage());
            throw new TutorServiceException("No se puede conectar con el servicio de validacion", "VALIDATION_CONNECTION_ERROR", e);
        } catch (WebClientResponseException e) {
            log.error("Error en respuesta del servicio de validacion: {} - {}", e.getStatusCode(), e.getMessage());
            throw new TutorServiceException("Error al validar la actividad: " + e.getMessage(), "VALIDATION_RESPONSE_ERROR", e);
        }
    }

    public ValidationResponse validateEvaluation(ValidateEvaluationRequest request) {
        log.info("Validando coherencia de diseno de evaluacion - actividad: {}", request.getNombreActividad());
        try {
            return tutorWebClient.post()
                    .uri("/validate-evaluation")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ValidationResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("Error de conexion con el servicio de validacion: {}", e.getMessage());
            throw new TutorServiceException("No se puede conectar con el servicio de validacion", "VALIDATION_CONNECTION_ERROR", e);
        } catch (WebClientResponseException e) {
            log.error("Error en respuesta del servicio de validacion: {} - {}", e.getStatusCode(), e.getMessage());
            throw new TutorServiceException("Error al validar la evaluacion: " + e.getMessage(), "VALIDATION_RESPONSE_ERROR", e);
        }
    }
}
