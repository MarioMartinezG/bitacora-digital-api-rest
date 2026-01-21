package com.diginexa.bitacora.exceptions.domain;

public class SolicitudSesionNotFoundException extends RuntimeException {

    public SolicitudSesionNotFoundException(Long id) {
        super("Solicitud de sesión no encontrada con ID: " + id);
    }

    public SolicitudSesionNotFoundException(String message) {
        super(message);
    }
}
