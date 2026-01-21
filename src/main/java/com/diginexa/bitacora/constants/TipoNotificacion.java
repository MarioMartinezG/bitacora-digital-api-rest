package com.diginexa.bitacora.constants;

/**
 * Tipos de notificación soportados por el sistema.
 */
public enum TipoNotificacion {
    VENCIMIENTO_PROXIMO("Vencimiento próximo de módulo"),
    SOLICITUD_SESION("Solicitud de sesión con tutor"),
    UMBRAL_ALCANZADO("Umbral de progreso alcanzado");

    private final String descripcion;

    TipoNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
