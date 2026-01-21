package com.diginexa.bitacora.exceptions.domain;

public class ConfiguracionNotFoundException extends RuntimeException {

    public ConfiguracionNotFoundException(String clave) {
        super("Configuración no encontrada con clave: " + clave);
    }

    public ConfiguracionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
