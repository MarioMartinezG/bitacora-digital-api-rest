package com.diginexa.bitacora.exceptions.domain;

public class CampoNotFoundException extends RuntimeException {
    public CampoNotFoundException(Long id) {
        super("Campo no encontrado con ID: " + id);
    }

    public CampoNotFoundException(String mensaje) {
        super(mensaje);
    }
}
