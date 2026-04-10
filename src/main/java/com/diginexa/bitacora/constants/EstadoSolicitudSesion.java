package com.diginexa.bitacora.constants;

/**
 * Estados posibles para una solicitud de sesión.
 */
public enum EstadoSolicitudSesion {
    PENDIENTE("Pendiente de respuesta"),
    ACEPTADA("Aceptada por el tutor"),
    RECHAZADA("Rechazada por el tutor"),
    COMPLETADA("Sesión completada");

    private final String descripcion;

    EstadoSolicitudSesion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
