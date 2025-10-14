package com.diginexa.bitacora.exceptions.domain;

public class ModuloNotFoundException extends RuntimeException {
    public ModuloNotFoundException(Long id) {
        super("Módulo no encontrado con ID: " + id);
    }

    public ModuloNotFoundException(String mensaje) {
        super(mensaje);
    }
}
