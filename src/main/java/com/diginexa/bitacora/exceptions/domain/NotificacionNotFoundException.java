package com.diginexa.bitacora.exceptions.domain;

public class NotificacionNotFoundException extends RuntimeException {

    public NotificacionNotFoundException(Long id) {
        super("Notificación no encontrada con ID: " + id);
    }

    public NotificacionNotFoundException(String message) {
        super(message);
    }
}
