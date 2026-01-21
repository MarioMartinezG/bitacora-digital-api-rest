package com.diginexa.bitacora.exceptions.domain;

public class CalendarioModuloNotFoundException extends RuntimeException {

    public CalendarioModuloNotFoundException(Long id) {
        super("Calendario de módulo no encontrado con ID: " + id);
    }

    public CalendarioModuloNotFoundException(String seccionCodigo) {
        super("Calendario de módulo no encontrado para sección: " + seccionCodigo);
    }
}
