package com.diginexa.bitacora.exceptions.domain;

public class SeccionNotFoundException extends RuntimeException {
    public SeccionNotFoundException(Long id) {
        super("Sección no encontrada con ID: " + id);
    }

    public SeccionNotFoundException(String mensaje) {
        super(mensaje);
    }
}
