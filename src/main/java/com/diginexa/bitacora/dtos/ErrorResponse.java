package com.diginexa.bitacora.dtos;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String error,
        int status,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String message, String error, int status) {
        return new ErrorResponse(message, error, status, LocalDateTime.now());
    }
}
