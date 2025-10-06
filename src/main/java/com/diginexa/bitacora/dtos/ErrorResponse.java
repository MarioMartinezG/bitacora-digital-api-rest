package com.diginexa.bitacora.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String message;
    private String error;
    private int status;
    private LocalDateTime timestamp;
    private Map<String, String> details;

    // Constructor para respuestas sin detalles
    public static ErrorResponse of(String message, String error, int status) {
        return new ErrorResponse(message, error, status, LocalDateTime.now(), null);
    }

    // Constructor para respuestas con detalles
    public static ErrorResponse withDetails(String message, String error, int status, Map<String, String> details) {
        return new ErrorResponse(message, error, status, LocalDateTime.now(), details);
    }
}
