package com.diginexa.bitacora.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MessageResponse {
    private String message;
    private LocalDateTime timestamp;

    public static MessageResponse of(String message) {
        return new MessageResponse(message, LocalDateTime.now());
    }
}
