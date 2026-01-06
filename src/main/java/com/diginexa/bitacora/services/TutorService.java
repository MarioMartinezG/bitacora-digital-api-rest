package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.tutor.TutorAskRequest;
import com.diginexa.bitacora.dtos.tutor.TutorAskResponse;
import com.diginexa.bitacora.dtos.tutor.TutorHealthResponse;
import com.diginexa.bitacora.dtos.tutor.TutorStatusResponse;
import com.diginexa.bitacora.exceptions.domain.TutorServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorService {

    private final WebClient tutorWebClient;

    public TutorStatusResponse getStatus() {
        log.info("Consultando estado del servicio de tutor inteligente");
        try {
            return tutorWebClient.get()
                    .uri("/status")
                    .retrieve()
                    .bodyToMono(TutorStatusResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("Error de conexion con el servicio de tutor: {}", e.getMessage());
            throw new TutorServiceException("No se puede conectar con el servicio de tutor inteligente", "TUTOR_CONNECTION_ERROR", e);
        } catch (WebClientResponseException e) {
            log.error("Error en respuesta del servicio de tutor: {} - {}", e.getStatusCode(), e.getMessage());
            throw new TutorServiceException("Error en el servicio de tutor: " + e.getMessage(), "TUTOR_RESPONSE_ERROR", e);
        }
    }

    public TutorHealthResponse getHealth() {
        log.info("Verificando salud del servicio de tutor inteligente");
        try {
            return tutorWebClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(TutorHealthResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("Error de conexion con el servicio de tutor: {}", e.getMessage());
            return TutorHealthResponse.builder()
                    .status("unhealthy")
                    .ollamaConnected(false)
                    .message("No se puede conectar con el servicio de tutor: " + e.getMessage())
                    .build();
        } catch (WebClientResponseException e) {
            log.error("Error en respuesta del servicio de tutor: {} - {}", e.getStatusCode(), e.getMessage());
            return TutorHealthResponse.builder()
                    .status("unhealthy")
                    .ollamaConnected(false)
                    .message("Error del servicio: " + e.getStatusCode())
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getModules() {
        log.info("Obteniendo modulos del curso desde el tutor inteligente");
        try {
            return tutorWebClient.get()
                    .uri("/modules")
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("Error de conexion con el servicio de tutor: {}", e.getMessage());
            throw new TutorServiceException("No se puede conectar con el servicio de tutor inteligente", "TUTOR_CONNECTION_ERROR", e);
        } catch (WebClientResponseException e) {
            log.error("Error en respuesta del servicio de tutor: {} - {}", e.getStatusCode(), e.getMessage());
            throw new TutorServiceException("Error al obtener modulos: " + e.getMessage(), "TUTOR_RESPONSE_ERROR", e);
        }
    }

    public TutorAskResponse ask(TutorAskRequest request) {
        log.info("Enviando pregunta al tutor inteligente - modulo: {}, usuario: {}", request.getModule(), request.getUserId());
        try {
            return tutorWebClient.post()
                    .uri("/ask")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TutorAskResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("Error de conexion con el servicio de tutor: {}", e.getMessage());
            throw new TutorServiceException("No se puede conectar con el servicio de tutor inteligente", "TUTOR_CONNECTION_ERROR", e);
        } catch (WebClientResponseException e) {
            log.error("Error en respuesta del servicio de tutor: {} - {}", e.getStatusCode(), e.getMessage());
            throw new TutorServiceException("Error al procesar la pregunta: " + e.getMessage(), "TUTOR_RESPONSE_ERROR", e);
        }
    }
}
