package com.diginexa.bitacora.services;

import com.diginexa.bitacora.dtos.tutor.TutorAskRequest;
import com.diginexa.bitacora.dtos.tutor.TutorAskResponse;
import com.diginexa.bitacora.dtos.tutor.TutorHealthResponse;
import com.diginexa.bitacora.dtos.tutor.TutorStatusResponse;
import com.diginexa.bitacora.dtos.tutor.DocumentListResponse;
import com.diginexa.bitacora.dtos.tutor.IndexTaskResponse;
import com.diginexa.bitacora.dtos.tutor.IndexStatusResponse;
import com.diginexa.bitacora.dtos.tutor.DocumentUploadResponse;
import com.diginexa.bitacora.exceptions.domain.TutorResourceNotFoundException;
import com.diginexa.bitacora.exceptions.domain.TutorServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorService {

    private final WebClient tutorWebClient;
    private final ObjectMapper objectMapper;

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

    public DocumentListResponse getDocuments() {
        log.info("Obteniendo lista de documentos del tutor inteligente");
        try {
            return tutorWebClient.get()
                    .uri("/documents")
                    .retrieve()
                    .bodyToMono(DocumentListResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("Error de conexion con el servicio de tutor: {}", e.getMessage());
            throw new TutorServiceException("No se puede conectar con el servicio de tutor inteligente", "TUTOR_CONNECTION_ERROR", e);
        } catch (WebClientResponseException e) {
            log.error("Error en respuesta del servicio de tutor: {} - {}", e.getStatusCode(), e.getMessage());
            throw new TutorServiceException("Error al obtener documentos: " + e.getMessage(), "TUTOR_RESPONSE_ERROR", e);
        }
    }

    public IndexTaskResponse startIndexing() {
        log.info("Iniciando proceso de indexacion de documentos");
        try {
            return tutorWebClient.post()
                    .uri("/index")
                    .retrieve()
                    .bodyToMono(IndexTaskResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("Error de conexion con el servicio de tutor: {}", e.getMessage());
            throw new TutorServiceException("No se puede conectar con el servicio de tutor inteligente", "TUTOR_CONNECTION_ERROR", e);
        } catch (WebClientResponseException e) {
            log.error("Error en respuesta del servicio de tutor: {} - {}", e.getStatusCode(), e.getMessage());
            throw new TutorServiceException("Error al iniciar indexacion: " + e.getMessage(), "TUTOR_RESPONSE_ERROR", e);
        }
    }

    public IndexStatusResponse getIndexStatus(String taskId) {
        log.info("Consultando estado de indexacion - taskId: {}", taskId);
        try {
            return tutorWebClient.get()
                    .uri("/index/status/{taskId}", taskId)
                    .retrieve()
                    .bodyToMono(IndexStatusResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("Error de conexion con el servicio de tutor: {}", e.getMessage());
            throw new TutorServiceException("No se puede conectar con el servicio de tutor inteligente", "TUTOR_CONNECTION_ERROR", e);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                String detail = extractDetailFromResponse(e.getResponseBodyAsString(), "Task not found: " + taskId);
                throw new TutorResourceNotFoundException(detail);
            }
            log.error("Error en respuesta del servicio de tutor: {} - {}", e.getStatusCode(), e.getMessage());
            throw new TutorServiceException("Error al obtener estado de indexacion: " + e.getMessage(), "TUTOR_RESPONSE_ERROR", e);
        }
    }

    private String extractDetailFromResponse(String responseBody, String defaultMessage) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.has("detail")) {
                return jsonNode.get("detail").asText();
            }
        } catch (Exception e) {
            log.warn("No se pudo parsear el cuerpo de la respuesta de error: {}", e.getMessage());
        }
        return defaultMessage;
    }

    public DocumentUploadResponse uploadDocuments(List<MultipartFile> files) {
        log.info("Subiendo {} documentos al tutor inteligente", files.size());
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            for (MultipartFile file : files) {
                builder.part("files", new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                }).contentType(MediaType.parseMediaType(
                        file.getContentType() != null ? file.getContentType() : "application/octet-stream"
                ));
            }

            return tutorWebClient.post()
                    .uri("/documents/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(DocumentUploadResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            log.error("Error de conexion con el servicio de tutor: {}", e.getMessage());
            throw new TutorServiceException("No se puede conectar con el servicio de tutor inteligente", "TUTOR_CONNECTION_ERROR", e);
        } catch (WebClientResponseException e) {
            log.error("Error en respuesta del servicio de tutor: {} - {}", e.getStatusCode(), e.getMessage());
            throw new TutorServiceException("Error al subir documentos: " + e.getMessage(), "TUTOR_RESPONSE_ERROR", e);
        } catch (IOException e) {
            log.error("Error al leer archivos: {}", e.getMessage());
            throw new TutorServiceException("Error al procesar archivos para subir", "FILE_READ_ERROR", e);
        }
    }
}
